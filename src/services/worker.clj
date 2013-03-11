(ns services.worker
  (:require [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as carmine-mq]
            [clojure.string :as string]
            [clojure.java.shell :refer [sh]]))

(def pool         (car/make-conn-pool))
(def spec-server1 (car/make-conn-spec))
(defmacro wcar [& body] `(car/with-conn pool spec-server1 ~@body))

(def projects-path "code/")

(defn app-path
  [project]
  (str projects-path project))

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
    (print-command (sh "bin/deploy" (app-path project) (deploy-branch branch)))))

(defn add-queue
  [msg]
  (let [{org :org repo :repo} msg]
    (println "received" org repo)
    (let [url (str "git@github.com:" org "/" repo ".git")]
      (print-command (sh "bin/add" (str projects-path repo) url)))))

(def deploy-worker
  (wcar (carmine-mq/make-dequeue-worker
   pool spec-server1 "deploy"
   :handler-fn deploy-queue)))

(def add-worker
  (wcar (carmine-mq/make-dequeue-worker
   pool spec-server1 "add"
   :handler-fn add-queue)))

(defn -main
  [& args]
  (while true ()))
