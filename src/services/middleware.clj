(ns services.middleware
  (:require [clojure.string :as string]
            [clojure.java.shell :refer [sh]]))

(defn- auth-token-valid?
  [auth-token project-name]
  (= "valid"
     (string/trim-newline
       (:out (sh "./bin/decrypt_token.sh" auth-token project-name)))))

(defn wrap-authentication
  [handler]
  (fn [request]
    (let [{params :params} request
          {auth-token :auth-token} params
          project-name (last (string/split (:uri request) #"/"))]
      (if (auth-token-valid? auth-token project-name)
        (handler request)
        {:status 401
         :headers {"Content-Type" "application/json"}
         :body "Not Authorized"}))))
