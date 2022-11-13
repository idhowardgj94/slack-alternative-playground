(ns howard.mattermost.client
  (:require
   [clj-http.client :as client]
   [clojure.data.json :as json]
   [howard.mattermost.store :refer [matter_most_url store]]))

(defn login
  "Login to server.
  it will login to the mattermost server,
  and save the auth inside store."
  [login_id password]
  (as-> (client/post (str matter_most_url "/api/v4/users/login") {:content-type :json
                                                                  :body (json/write-str {:login_id login_id
                                                                                         :password password
                                                                                         :device-id ""
                                                                                         :token ""})}) %
    (swap! store (fn [s]
                   (let [body (json/read-json (:body %))]
                   (assoc s :token (-> %
                                       :headers
                                       (get "token"))
                          :userid (:id body)
                          :email (:email body)))))))

(defn get-bearer-token
  "This function take a token, and response
  map contain :bearer <bearer token>"
  [token]
  {:Authorization (str "Bearer " token)})

(defn create-user
  "This function create user using admin account.
  make sure to call login to login admin account first.
  if success login to admin account, store will contain token,
  which will used to bearer token."
  [email username password]
  (client/post (str matter_most_url "/api/v4/users") {:content-type :json
                                                    :headers (get-bearer-token (:token @store))
                                                    :body (json/write-str {:email email
                                                                           :username username
                                                                           :password password})}))

(defn call-with-auth!
  "call the mattermost client with auth.
  it will use store, so you need to call login first
  default url is base url."
  ([method path params]
   (case method
     :get (client/get (str matter_most_url path) {:content-type :json
                                                          :headers (get-bearer-token (:token @store))
                                                          :query-params params})
     :post (client/post (str matter_most_url path) {:content-type :json
                                                    :headers (get-bearer-token (:token @store))
                                                    :body (json/write-str params)})
     :put (client/get (str matter_most_url path) {:content-type :json
                                                          :headers (get-bearer-token (:token @store))
                                                          :body (json/write-str params)})
     :delete (client/delete (str matter_most_url path) {:content-type :json
                                                             :headers (get-bearer-token (:token @store))
                                                             :body (json/write-str params)})))
  ([method path]
   (call-with-auth! method path {})))

;; this is how to call channel
;; create a dirrect chanenl
;; response may contain channel_id
(defn create-direct-room
  "create direct room by two user id
  remember res.body.id
  "
  [userid otherid]
  (call-with-auth! :post "/api/v4/channels/direct"
                   [userid
                    otherid]))

(defn send-message
  "send a message to a room by channel_id"
  [channelid message]
  (call-with-auth! :post "/api/v4/posts"
                   {:channel_id channelid
                    :message message}))

;; shift-G: end of flie
;; gg: begin of file
(defn get-user-list
  "get user lists from mattermost"
  []
  (let [users (-> (call-with-auth! :get "/api/v4/users")
                  :body
                  (json/read-json))
        reslist (transient [])]
    (swap! store #(assoc % :users users))
    (doseq [{:keys [email username id]} users]
      (conj! reslist {:email email
                      :username username
                      :id id}))
    (persistent! reslist)))

(defn set-direct-message-show
  "set the direct message to show"
  [userid otherid]
  (call-with-auth! :put (str "/api/v4/users/"  userid "/preferences")
                   {:category "direct_channel_show"
                    :name  otherid
                    :user_id  userid
                    :value true}))

(defn create-a-channel
  "to keep simple, we will treat name and display_name in same"
  [teamid name type]
  (call-with-auth! :post "/api/v4/channels"
                   {:team_id teamid
                    :name name
                    :display_name name
                    :type type}))
(defn add-user-to-channel
  "add a user to a channel by channel id"
  [channelid userid]
  (call-with-auth! :post (str "/api/v4/channels/" channelid "/members")
                   {:channel_id channelid
                    :user_id userid}))

;; no use
(defn get-teams
  "store need to be set to true to use this functionality"
  []
  (call-with-auth! :get "/api/v4/teams"))

(defn get-me-teams-unread
  []
  (call-with-auth! :get "/api/v4/users/me/teams/unread" {:include_collapsed_threads true}))
