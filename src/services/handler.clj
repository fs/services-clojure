(ns services.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :as string]
            [cheshire.core :refer :all]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as carmine-mq]))

(def pool         (car/make-conn-pool))
(def spec-server1 (car/make-conn-spec))
(defmacro wcar [& body] `(car/with-conn pool spec-server1 ~@body))

(defn enqueue
  [queue message]
  (wcar (carmine-mq/enqueue queue message)))

(defn add
  [org repo]
  (enqueue "add" {:org org :repo repo})
  "enqueued")

(defn deploy
  [project branch]
  (enqueue "deploy" {:project project :branch branch})
  "enqueued")

(defn ci-hook
  [params]
  (let [response (parse-stream (clojure.java.io/reader params))]
    (if (and (= "passed" (response "result")) (= "develop" (response "branch_name")))
      (deploy (response "project_name") "develop")
      (generate-string response))))

(defroutes app
  (POST "/deploy/:project" [project :as {{branch :branch} :params}]
       (deploy project branch))
  (POST "/add/:org/:repo" [org repo]
       (add org repo))
  (POST "/ci" {params :body}
        (ci-hook params))
  (route/not-found "Not Found"))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (handler/api app)
                     {:port port :join? false})))
