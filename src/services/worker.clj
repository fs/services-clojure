(ns services.worker
  (:require [clojure.string :as string]
            [services.redis-support :refer :all]
            [clojure.java.shell :refer [sh]]))

(def deploy-script-format "sudo -u %s -i /home/%s/application/script/deploy")

(defn deploy-script
  [project]
  (format deploy-script-format project project))

(defn deploy-branch
  [branch]
  (if (empty? branch)
    "master"
    branch))

(defn print-command
  [{out :out err :err}]
  (if (string/blank? out)
    (println (string/trim-newline err))
    (println (string/trim-newline out))))

(defn deploy-queue
  [msg]
  (let [{project :project branch :branch} msg]
    (println "received" project branch)
    (print-command (sh (deploy-script project) (deploy-branch branch)))))

(create-worker "deploy" deploy-queue)

(defn -main
  [& args]
  (while true ()))
