(defproject chart-destroyer "0.1.0"
  :description "A Clojure application to delete old charts in VMware Harbor"
  :url "https://github.com/savemech/chart-destroyer"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [clj-http "3.12.0"]
                 [clj-time "0.15.2"]
                 [clojure.tools.cli "1.0.194"]
                 [environ "1.1.0"]]
  :main chart-destroyer.core)
