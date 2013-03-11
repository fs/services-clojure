(ns services.redis-support
  (:require [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as carmine-mq]))

(def pool         (car/make-conn-pool))
(def spec-server1 (car/make-conn-spec))
(defmacro wcar [& body] `(car/with-conn pool spec-server1 ~@body))

(defn enqueue
  [queue message]
  (wcar (carmine-mq/enqueue queue message)))

(defmacro create-worker
  [queue handler-fn]
  `(wcar 
    (carmine-mq/make-dequeue-worker pool spec-server1 ~queue
     :handler-fn ~handler-fn)))
