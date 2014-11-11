(defproject agamate "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.immutant/web "2.0.0-alpha2"]
                 [org.immutant/scheduling "2.0.0-alpha2"]
                 [compojure "1.2.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring/ring-devel "1.3.1"]
                 [ring-middleware-format "0.4.0"]
                 [korma "0.4.0"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]]
  :main ^:skip-aot agamate.core
  :target-path "target/%s"
  :plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]]
  :profiles {:uberjar {:aot :all}})
