(ns chart-destroyer.core
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as str]
   [clj-http.client :as http]
   [clj-time.core :as time]
   [clj-time.format :as time-format]
   [clojure.core.async :as async]
   [clojure.java.io :as io]
   [environ.core :refer [env]]))

(defonce cache (atom {}))

(defn parse-cli-opts [args]
  [["-d" "--dry-run" "Dry run mode"]
   ["-t" "--threshold DAYS" "Threshold for deletion (days)"]])

(defn get-projects []
  (let [harbor-url (env :HARBOR_URL)
        harbor-token (env :HARBOR_TOKEN)
        url (str harbor-url "/api/v2.0/projects")
        headers {"Accept" "application/json, text/plain, */*"
                 "Authorization" (str "Bearer " harbor-token)}
        response (http/get url {:headers headers})]
    (if (= (:status response) 200)
      (:body response)
      (throw (Exception. "Failed to fetch projects from Harbor.")))))

(defn get-repos-charts [project-id]
  (let [harbor-url (env :HARBOR_URL)
        harbor-token (env :HARBOR_TOKEN)
        url (str harbor-url "/api/v2.0/projects/" project-id "/repositories")
        headers {"Accept" "application/json, text/plain, */*"
                 "Authorization" (str "Bearer " harbor-token)}
        response (http/get url {:headers headers})]
    (if (= (:status response) 200)
      (:body response)
      (throw (Exception. (str "Failed to fetch repositories for project ID " project-id))))))

(defn delete-chart [chart-id]
  (let [harbor-url (env :HARBOR_URL)
        harbor-token (env :HARBOR_TOKEN)
        url (str harbor-url "/api/v2.0/charts/artifacts/" chart-id)
        headers {"Authorization" (str "Bearer " harbor-token)}
        response (http/delete url {:headers headers})]
    (if (= (:status response) 200)
      (println "Deleted chart with ID" chart-id)
      (println "Failed to delete chart with ID" chart-id))))

(defn delete-old-charts [project-id threshold]
  (let [repos-charts (get-repos-charts project-id)
        current-time (time/now)
        threshold-seconds (* threshold 24 60 60)]
    (doseq [repo-chart (json/read-str repos-charts :key-fn keyword)]
      (let [chart-id (:digest repo-chart)
            timestamp (-> repo-chart :update_time
                          (time-format/parse (time-format/formatter "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))]
        (if (>= (time/minus current-time timestamp) threshold-seconds)
          (do
            (println "Deleting chart with ID" chart-id)
            (delete-chart chart-id))
          (println "Chart with ID" chart-id "is not older than the threshold."))))))

(defn -main [& args]
  (let [opts (parse-cli-opts args)
        dry-run? (:dry-run opts)
        threshold (if (and (contains? opts :threshold) (number? (read-string (:threshold opts))))
                    (read-string (:threshold opts))
                    nil)]
    (if dry-run?
      (println "Dry run mode activated.")
      (if threshold
        (let [projects (get-projects)]
          (doseq [project (json/read-str projects :key-fn keyword)]
            (let [project-id (:project_id project)]
              (println "Processing project with ID" project-id)
              (delete-old-charts project-id threshold))))
        (println "Threshold not provided. Please specify a threshold with -t/--threshold.")))))

(defn -main-cli [& args]
  (environ/bind-env :HARBOR_URL "YOUR_HARBOR_URL_HERE")
  (environ/bind-env :HARBOR_TOKEN "YOUR_HARBOR_TOKEN_HERE")
  (-main args))
