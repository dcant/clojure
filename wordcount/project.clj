(defproject wordcount "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main wordcount.main
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.6"]]
  :profiles {:dev {:dependencies [[storm/storm-core "0.9.0.1"]]}}
  :plugins [[lein2-eclipse "2.0.0"]]
  :aot [wordcount.main])
