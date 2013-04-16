(ns services.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :as string]
            [cheshire.core :refer :all]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [services.redis-support :refer :all]))

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
  (POST "/ci" {params :body}
        (ci-hook params))
  (route/not-found "Not Found"))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (handler/api app)
                     {:port port :join? false})))
