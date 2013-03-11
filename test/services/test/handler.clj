(ns services.test.handler
  (:use clojure.test
        ring.mock.request  
        services.handler
        services.test.fixtures)
  (:require [cheshire.core :refer :all]))

(defn json-ci-payload
  [branch]
  (generate-string (ci-payload branch)))

(defn ci-request
  [branch]
  (body (content-type (request :post "/ci") "application/json") (json-ci-payload branch)))

(deftest test-app
  (testing "/ci"
    (let [response (app (ci-request "master"))
          json-response (parse-string (:body response))]
      (is (= (:status response) 200))
      (is (= json-response (ci-payload "master"))))
    (let [response (app (ci-request "develop"))]
      (is (= (:status response) 200))
      (is (= (:body response) "enqueued"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
