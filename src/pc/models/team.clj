(ns pc.models.team
  (:require [datomic.api :as d]
            [pc.datomic :as pcd]))

;; We'll pretend we have a type here
#_(t/def-alias Team (HMap :mandatory {:team/subdomain String
                                      :db/id Long
                                      ;; probably a uuid type
                                      :team/uuid String}))

(defn find-by-subdomain [db subdomain]
  (some->> subdomain
    (d/datoms db :avet :team/subdomain)
    first
    :e
    (d/entity db)))

(defn create-for-subdomain! [subdomain annotations]
  @(d/transact (pcd/conn) [(merge {:db/id (d/tempid :db.part/tx)}
                                  annotations)
                           {:db/id (d/tempid :db.part/user)
                            :team/subdomain subdomain
                            :team/uuid (d/squuid)}]))

(defn public-read-api [team]
  (select-keys team [:team/subdomain :team/uuid]))