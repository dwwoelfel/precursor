(ns pc.http.plan
  "Provides helpers for plan operations that need to happen during an http request"
  (:require [clj-time.core :as time]
            [clj-time.coerce]
            [pc.stripe :as stripe]))


(defn stripe-customer->plan-fields [stripe-customer]
  (let [card-fields (-> stripe-customer (get-in ["sources" "data"]) first)
        subscription (-> stripe-customer (get-in ["subscriptions" "data"]) first)
        discount-fields (-> stripe-customer (get-in ["discount"]))]
    (merge
     (stripe/card-api->model card-fields)
     {:plan/start (-> subscription
                    (get "start")
                    (* 1000)
                    clj-time.coerce/from-long
                    clj-time.coerce/to-date)
      :plan/stripe-subscription-id (get subscription "id")
      :plan/stripe-customer-id (get stripe-customer "id")}
     (when (seq discount-fields)
       (stripe/discount-api->model discount-fields)))))

(defn create-stripe-customer
  "Creates Stripe customer and new subscription from token generated by Checkout.js"
  [team cust token-id]
  (let [plan (:team/plan team) ;; XXX: plans in db
        stripe-customer (stripe/create-customer token-id
                                                "team"
                                                (or (:plan/trial-end plan)
                                                    ;; XXX: just until we have plans with trial-ends
                                                    (time/now))
                                                :email (:cust/email cust)
                                                :coupon-code (:plan/coupon-code plan)
                                                :description (format "Team plan for %s, created by %s"
                                                                     (:team/subdomain team)
                                                                     (:cust/email cust)))]
    stripe-customer))

(defn update-card
  "Creates Stripe customer and new subscription from token generated by Checkout.js"
  [team token-id]
  (let [plan (:team/plan team) ;; XXX: plans in db
        stripe-customer (stripe/update-card (:plan/stripe-customer-id plan) token-id)]
    stripe-customer))