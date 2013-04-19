(ns services.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :as string]
            [cheshire.core :refer :all]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [services.redis-support :refer :all]))

(defn deploy
  [project branch]
  (enqueue "deploy" {:project project :branch branch})
  "enqueued")

(defn- passed?
  [response]
  (= "passed" (response "result")))

(defn ci-hook
  [body deploy-branches]
  (let [response (parse-stream (clojure.java.io/reader body))
        branch (response "branch_name")]
    (if (and (passed? response) (deploy-branches branch))
      (deploy (response "project_name") branch)
      (generate-string response))))

(defroutes app
  (POST "/deploy/:project" [project :as {{branch :branch} :params}]
       (deploy project branch))
  (POST "/ci" {body :body { deploy-branches :deploy-branches} :query-params}
        (ci-hook body (set (string/split deploy-branches #","))))
  (route/not-found "Not Found"))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (handler/api (-> #'app
                                      wrap-keyword-params))
                     {:port port :join? false})))
