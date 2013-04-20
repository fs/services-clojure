(ns services.middleware
  (:require [clojure.string :as string]
            [clojure.java.shell :refer [sh]]))

(defn- auth-token-valid?
  [auth-token]
  (= "valid" (string/trim-newline (:out (sh "./bin/decrypt_token.sh" auth-token)))))

(defn wrap-authentication
  [handler]
  (fn [request]
    (let [{params :params} request
          {auth-token :auth-token} params]
      (if (auth-token-valid? auth-token)
        (handler request)
        {:status 401
         :headers {"Content-Type" "application/json"}
         :body "Not Authorized"}))))
