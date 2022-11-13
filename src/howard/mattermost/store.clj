(ns howard.mattermost.store)

(def header-json {:headers {"content-type" "application/json"}})
(def matter_most_url "http://localhost:8065")
(def store (atom {:token ""
                  :body {}
                  :teams []
                  :team_id ""
                  :team_name "'"}))

(def fake-data-list
  [{:email "idhowardgj94@gmail.com"
    :username "howardchang"
    :password "12345678"}
   {:email "rtkao@tsmc.com"
    :username "rtkao"
    :password "12345678"}
   {:email "eva1219@gmail.com"
    :username "evawang"
    :password "12345678"}
   {:email "patrice0813@gmail.com"
    :username "patricejhang"
    :password "12345678"}
   {:email "joshlu@gmail.com"
    :username "jushlu"
    :password "12345678"}
   {:email "tubemaster@gmail.com"
    :username "tubemaster"
    :password "12345678"}
   {:email "fathertu@gmail.com"
    :username "fathertu"
    :password "12345678"}
   {:email "neighbor@gmail.com"
    :username "neightbor"
    :password "12345678"}
   {:email "evachang@gmail.com"
    :username "evachang"
    :password "12345678"}])
