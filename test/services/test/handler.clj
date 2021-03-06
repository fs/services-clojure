(ns services.test.handler
  (:use clojure.test
        ring.mock.request  
        services.handler
        services.test.fixtures)
  (:require [cheshire.core :refer :all]
            [clojure.string :as string]))

(defn json-ci-payload
  [branch status]
  (generate-string (ci-payload branch status)))

(defn query-params
  [request params]
  (assoc request :query-params params))

(defn ci-request
  [deploy-branches branch status]
  (body (content-type (-> (request :post "/ci")
                          (query-params {:deploy-branches
                                         (string/join "," deploy-branches)}))
                      "application/json")
        (json-ci-payload branch status)))

(deftest test-app
  (testing "/ci"
    (let [response (app (ci-request ["develop"] "master" "passed"))
          json-response (parse-string (:body response))]
      (is (= (:status response) 200))
      (is (= json-response (ci-payload "master" "passed"))))

    (let [response (app (ci-request  ["develop" "master"] "develop" "passed"))]
      (is (= (:status response) 200))
      (is (= (:body response) "enqueued"))))

    (let [response (app (ci-request ["develop"] "develop", "failed"))
          json-response (parse-string (:body response))]
      (is (= (:status response) 200))
      (is (= json-response (ci-payload "develop" "failed"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
