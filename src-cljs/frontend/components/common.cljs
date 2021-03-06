(ns frontend.components.common
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [frontend.async :refer [put!]]
            [frontend.auth :as auth]
            [frontend.datetime :as datetime]
            [frontend.utils :as utils :include-macros true]
            [goog.dom.DomHelper]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [sablono.core :refer [html]]))

(def icon-paths
  {
   :stroke-precursor "M35.7,45.3V95 M60.5,84.4C82,78.6,94.8,56.5,89,34.9C83.2,13.4,61.1,0.6,39.5,6.4S5.2,34.3,11,55.9"
   :stroke-download "M5,95 h90 M5,85v10 M95,85v10 M50,5v70 M50,75l30-30 M20,45l30,30"
   :stroke-check "M35,80 L5,50 M95,20L35,80"
   :stroke-times "M82.5,82.5l-65-65 M82.5,17.5l-65,65"
   :stroke-plus "M50,72.5v-45 M27.5,50h45"
   :stroke-minus "M27.5,50h45"
   :stroke-cursor "M23.3,80.4V5 l53.3,53.3c0,0-21.5,0-21.5,0s12.4,29.8,12.4,29.8L50.9,95c0,0-12.4-29.8-12.4-29.8S23.3,80.4,23.3,80.4z"
   :stroke-border-circle "M95,50c0,24.9-20.1,45-45,45S5,74.9,5,50S25.1,5,50,5 S95,25.1,95,50z"
   :stroke-border-square "M95,95H5V5h90V95z"
   :stroke-rectangle "M87.5,87.5h-75v-75h75V87.5z M20,5H5v15h15V5z M95,5H80v15h15V5z M20,80H5v15h15V80z M95,80H80v15h15V80z"
   :stroke-line "M95,20H80V5h15V20z M20,80H5v15h15V80z M87.5,12.5l-75,75"
   :stroke-text "M65.9,92.4H34.1H50 V7.6 M95,21.4c0,0-7.9-13.8-7.9-13.8c0,0-74.1,0-74.1,0L5,21.4"
   :stroke-pencil "M89.5,10.5c3.9,3.9,6.3,7.8,5.3,8.8L24.3,89.8L5,95l5.2-19.3L80.7,5.2C81.7,4.2,85.6,6.6,89.5,10.5z M22.5,88.1L11.9,77.5 M81.3,8.1c0.9,1.7,2.6,3.8,4.7,5.9c2.1,2.1,4.2,3.8,5.9,4.7 M70.3,19.1c0.9,1.7,2.6,3.8,4.7,5.9c2.1,2.1,4.2,3.8,5.9,4.7 M68.3,21.1c0.9,1.7,2.6,3.8,4.7,5.9c2.1,2.1,4.2,3.8,5.9,4.7"
   :stroke-ellipse "M57.5,5h-15v15h15V5z M95,42.5H80v15h15V42.5z M20,42.5H5v15h15V42.5z M57.5,80h-15v15h15V80z M87.5,50c0,20.7-16.8,37.5-37.5,37.5 S12.5,70.7,12.5,50S29.3,12.5,50,12.5S87.5,29.3,87.5,50z"
   :stroke-user "M75,30 c0,13.8-11.2,25-25,25S25,43.8,25,30S36.2,5,50,5S75,16.2,75,30z M24.1,49.6C15.5,56.9,10,67.8,10,80c0,8.3,17.9,15,40,15 s40-6.7,40-15c0-12.2-5.5-23.1-14.1-30.4"
   :stroke-users "M59.2,40.8c0,11.5-9.3,20.8-20.8,20.8s-20.8-9.3-20.8-20.8S26.8,20,38.3,20S59.2,29.3,59.2,40.8z M16.7,57.1 C9.5,63.3,5,72.3,5,82.5C5,89.4,19.9,95,38.3,95s33.3-5.6,33.3-12.5c0-10.2-4.5-19.2-11.7-25.4 M64.8,46.4 c10-1.5,17.7-10.1,17.7-20.6C82.5,14.3,73.2,5,61.7,5c-7.3,0-13.7,3.8-17.5,9.5 M77.4,78.5c10.5-2.1,17.6-6.3,17.6-11 c0-10.2-4.5-19.2-11.7-25.4"
   :stroke-dot "M49.5,50a0.5,0.5 0 1,0 1,0a0.5,0.5 0 1,0 -1,0"
   :stroke-chats "M60.8,63.2c-0.2,12-12.9,21.5-28.3,21.2 c-3.5-0.1-6.8-0.6-9.8-1.6L8.5,88.8c0,0,4.8-9.7,0.4-15.3c-2.6-3.4-4-7.3-3.9-11.4c0.2-12,12.9-21.5,28.3-21.2 C48.7,41.2,61,51.2,60.8,63.2z M68.5,73.9L89.7,83c0,0-7.2-14.5-0.6-22.9c3.8-5,6-10.9,5.9-17.2c-0.4-18-19.4-32.2-42.5-31.7 c-19.1,0.4-35,10.7-39.8,24.4"
   :stroke-crosshair "M50,5v90 M5,50h90"
   :stroke-ibeam "M50,10v80 M41.3,95h-10 M68.7,95h-10 M45,50h10 M50,90 l-8.7,5 M50,90l8.7,5 M58.7,5h10 M31.3,5h10 M58.7,5L50,10 M41.3,5l8.7,5"
   :stroke-blog "M85,32.5H35 M35,50h50 M35,67.5h50 M25,85h60 c5.5,0,10-4.5,10-10V15H25v60c0,5.5-4.5,10-10,10S5,80.5,5,75V35h15"
   :stroke-clock "M95,50c0,24.9-20.1,45-45,45S5,74.9,5,50S25.1,5,50,5 S95,25.1,95,50z M71.2,71.2C71.2,71.2,50,50,50,50V20"
   :stroke-star "M50,7.2l13.9,28.2L95,39.9L72.5,61.8l5.3,31L50,78.2 L22.2,92.8l5.3-31L5,39.9l31.1-4.5L50,7.2z"
   :stroke-delete "M35,80L5,50c0,0,30-30,30-30s60,0,60,0v60 C95,80,35,80,35,80z M45,35l30,30 M75,35L45,65"
   :stroke-shift "M5,52L50,7l45,45H70.5V93H29.5V52H5z"
   :stroke-option "M95,15H65 M95,85H75L35,15H5"
   :stroke-control "M86,41L50,5L14,41"
   :stroke-command "M65,35v30H35V35H65z M20,5C11.7,5,5,11.7,5,20 c0,8.3,6.7,15,15,15h15V20C35,11.7,28.3,5,20,5z M95,20c0-8.3-6.7-15-15-15c-8.3,0-15,6.7-15,15v15h15C88.3,35,95,28.3,95,20z M5,80c0,8.3,6.7,15,15,15c8.3,0,15-6.7,15-15V65H20C11.7,65,5,71.7,5,80z M80,65H65v15c0,8.3,6.7,15,15,15c8.3,0,15-6.7,15-15 C95,71.7,88.3,65,80,65z"
   :stroke-esc "M95,46.6c-3.2-3.2-7.4-4.8-11.7-4.8 c-9.1,0-16.5,7.4-16.5,16.5s7.4,16.5,16.5,16.5c4.2,0,8.4-1.6,11.7-4.8 M40.4,72.3c1.6,1.6,3.7,2.4,5.8,2.4c4.6,0,8.2-3.7,8.2-8.2 s-3.7-8.2-8.2-8.2S38,54.6,38,50s3.7-8.2,8.2-8.2c2.1,0,4.2,0.8,5.8,2.4 M29.7,74.7H5c0,0,0-49.5,0-49.5h24.7 M5,50h18.6"
   :stroke-space "M95,65v30H5V65"
   :stroke-click-1 "M29.2,30h-24"
   :stroke-click-2 "M31.8,23.6L19.1,10.9"
   :stroke-click-3 "M38.2,21V9"
   :stroke-private "M72.5,50V37.5 C72.5,25.1,62.4,15,50,15S27.5,25.1,27.5,37.5V50"
   :stroke-private-bottom "M80,95H20V50h60V95z"
   :stroke-public "M65,95H5V50h60V95z M95,50V27.5 C95,15.1,84.9,5,72.5,5S50,15.1,50,27.5V50"
   :stroke-mouse "M58,5h17c2.8,0,5,2.2,5,5v55c0,16.6-13.4,30-30,30 S20,81.6,20,65V10c0-2.8,2.2-5,5-5h20c2.8,0,5,2.2,5,5c0,0,0,25,0,25"
   :stroke-globe "M95,50c0,24.9-20.1,45-45,45S5,74.9,5,50S25.1,5,50,5 S95,25.1,95,50z M16.5,20c8.2,4.6,20.2,7.5,33.5,7.5s25.3-2.9,33.5-7.5 M83.5,80c-8.2-4.6-20.2-7.5-33.5-7.5S24.7,75.4,16.5,80 M50,5C37.6,5,27.5,25.1,27.5,50S37.6,95,50,95s22.5-20.1,22.5-45S62.4,5,50,5z M5,50h90 M50,95V5"
   :stroke-newdoc "M58,80V50 M43,65h30 M12,77l0-52L32,5l34,0v20 M58,35 c-16.6,0-30,13.4-30,30s13.4,30,30,30s30-13.4,30-30S74.6,35,58,35z M12,25h20V5"
   :stroke-doc "M84,94H16V6h68V94z"
   :stroke-docs "M87.5,95h-51V29h51V95z M63.5,29V5h-51v66h24"
   :stroke-docs-team "M63.5,95h-51V29h51V95z M63.5,71h24V5h-51v24"
   :stroke-login "M35,82.1V70 M5,43.6h42.9 M30.7,60.7l17.1-17.1 L30.7,26.4 M35,17.1V5h60c0,0,0,77.1,0,77.1L52.1,95V17.9L88.3,7"
   :stroke-logout "M5,82.1 M65,55v27.1 M95,43.6H52.1 M77.9,60.7L95,43.6 L77.9,26.4 M65,32.1V5H5c0,0,0,77.1,0,77.1L47.9,95V17.9L11.5,7"
   :stroke-info "M50,40v35 M59,66l-9,9 M41,49l9-9 M50,25 c-1.4,0-2.5,1.1-2.5,2.5S48.6,30,50,30s2.5-1.1,2.5-2.5S51.4,25,50,25z M95,50c0,24.9-20.1,45-45,45S5,74.9,5,50S25.1,5,50,5 S95,25.1,95,50z"
   :stroke-cog "M94.8,54.3c-0.3,2.1-1.9,3.8-3.9,4c-2.5,0.3-7.7,0.9-7.7,0.9c-2.3,0.5-3.9,2.5-3.9,4.9c0,1,0.3,2,0.8,2.7c0,0.1,3.1,4.1,4.7,6.2 c1.3,1.6,1.2,3.9-0.1,5.5c-1.8,2.3-3.8,4.3-6.1,6.1c-0.8,0.7-1.8,1-2.8,1c-0.9,0-2-0.3-2.7-0.9L67,80.1c-0.7-0.6-1.8-0.8-2.8-0.8 c-2.4,0-4.4,1.8-4.9,4.1l-0.9,7.5c-0.3,2.1-2,3.7-4,3.9C52.9,94.9,51.4,95,50,95c-1.4,0-2.9-0.1-4.3-0.2c-2.1-0.3-3.7-1.9-4-3.9 c0,0-0.9-7.4-0.9-7.5c-0.4-2.3-2.4-4.1-4.9-4.1c-1.1,0-2.2,0.4-3,0.9L27,84.8c-0.7,0.7-1.8,0.9-2.7,0.9c-1,0-2-0.4-2.8-1 c-2.3-1.8-4.3-3.8-6.1-6.1c-1.3-1.6-1.4-3.9-0.1-5.5l4.5-5.9c0.7-0.8,1-1.9,1-3c0-2.5-1.9-4.6-4.3-4.9l-7.3-0.9 c-2.1-0.3-3.7-2-3.9-4c-0.3-2.8-0.3-5.7,0-8.6c0.2-2.1,1.9-3.7,3.9-4l7.3-0.9c2.4-0.4,4.3-2.4,4.3-5c0-1-0.4-2.1-1-2.9 c0,0-3-3.9-4.5-5.9c-1.3-1.6-1.3-3.9,0.1-5.5c1.8-2.3,3.8-4.3,6.1-6.1c1.6-1.3,3.9-1.4,5.5-0.1l5.9,4.6c0.8,0.6,1.9,0.9,3,0.9 c2.4,0,4.5-1.8,4.9-4.1l0.9-7.5c0.3-2.1,2-3.7,4-3.9c2.8-0.3,5.7-0.3,8.6,0c2.1,0.3,3.7,1.9,4,3.9l0.9,7.5c0.5,2.3,2.4,4.1,4.9,4.1 c1,0,2-0.4,2.8-0.8c0,0,4-3.1,6.1-4.7c1.6-1.3,3.9-1.2,5.5,0.1c2.3,1.8,4.3,3.8,6.1,6.1c1.3,1.6,1.4,3.9,0.1,5.5 c0,0-4.7,6.1-4.7,6.2c-0.6,0.7-0.8,1.7-0.8,2.6c0,2.4,1.7,4.4,3.9,5c0,0,5.2,0.7,7.7,0.9c2.1,0.3,3.7,2,3.9,4 C95.1,48.5,95.1,51.4,94.8,54.3z"
   :stroke-home "M95,95H5 V50L50,5l45,45V95z M80,30V5"
   :stroke-hash "M54.7,95L72.2,5 M46.7,5L29.3,95 M20,37h65 M15,67h65"
   :stroke-team "M61.8,38.2c13,13,13,34,0,47s-34,13-47,0s-13-34,0-47S48.8,25.3,61.8,38.2z M38.2,14.7c-13,13-13,34,0,47s34,13,47,0s13-34,0-47 S51.2,1.8,38.2,14.7z"
   :stroke-arrow-up "M50,95V5 M86,41L50,5L14,41"
   :stroke-arrow-down "M50,95V5 M14,59l36,36l36-36"
   :stroke-arrow-right "M95,50H5 M59,86l36-36L59,14"
   :stroke-arrow-left "M95,50H5 M41,14L5,50l36,36"
   :stroke-menu-top "M5,25h90"
   :stroke-menu-mid "M5,50h90"
   :stroke-menu-btm "M5,75h90"
   :stroke-read-only "M50,25.9c-18.8,0-35.3,9.6-45,24.1 c9.7,14.5,26.2,24.1,45,24.1S85.3,64.5,95,50C85.3,35.5,68.8,25.9,50,25.9z"
   :stroke-lock-top "M75,45V30C75,16.2,63.8,5,50,5S25,16.2,25,30v15"
   :stroke-chat "M50,5c24.9,0,45,20.1,45,45c0,24.9-20.1,45-45,45H5V50 C5,25.1,25.1,5,50,5z"
   :stroke-sound "M50,18.2l0,63.6L33.2,65h-15V35h15L50,18.2z"
   :stroke-wave1 "M60.6,60.6C63.3,57.9,65,54.1,65,50s-1.7-7.9-4.4-10.6"
   :stroke-wave2 "M71.2,71.2C76.6,65.8,80,58.3,80,50 c0-8.3-3.4-15.8-8.8-21.2"
   :stroke-wave3 "M81.8,81.8C90,73.7,95,62.4,95,50 c0-12.4-5-23.7-13.2-31.8"
   :stroke-mic "M80,50c0,16.6-13.4,30-30,30S20,66.6,20,50 M50,5 c-8.3,0-15,6.7-15,15v30c0,8.3,6.7,15,15,15c8.3,0,15-6.7,15-15V20C65,11.7,58.3,5,50,5z M50,80v15 M65,95H35"
   :stroke-slash-forward "M5,95L95,5"
   :stroke-slash-backward "M5,5l90,90"
   :stroke-at "M70,50c0,11-9,20-20,20s-20-9-20-20s9-20,20-20 S70,39,70,50z M70,25v50 M90.3,70c3-6,4.7-12.8,4.7-20C95,25.1,74.9,5,50,5S5,25.1,5,50c0,24.9,20.1,45,45,45c7.2,0,14-1.7,20-4.7"
   :stroke-sharing "M50,5h45c0,0,0,45,0,45 M50,50L95,5 M27.5,5H5v90 c0,0,90,0,90,0V72.5"
   :stroke-email "M95,35v60H5V35L50,5L95,35z M95,35L5,95 M5,35l90,60"
   :stroke-phone "M70,95 H30c-5.5,0-10-4.5-10-10V15c0-5.5,4.5-10,10-10h40c5.5,0,10,4.5,10,10v70C80,90.5,75.5,95,70,95z M50.8,70.1 c-3.4-0.5-6.2,2.4-5.7,5.7c0.3,2.1,2,3.8,4.1,4.1c3.4,0.5,6.2-2.4,5.7-5.7C54.6,72.1,52.9,70.4,50.8,70.1z M65,20H35v35h30V20z"
   :stroke-activity "M92.1,34.1c0,0-31.8,31.8-31.8,31.8L39.1,44.7L17.9,65.9 M5,5v90h90"
   :stroke-credit "M95,80H5V20h90V80z M5,35h90 M15,65h50"
   :stroke-heart "M88.4,50c8.8-8.8,8.8-23,0-31.8s-23-8.8-31.8,0 C52.2,22.6,50,28.3,50,34.1c0-5.8-2.2-11.5-6.6-15.9c-8.8-8.8-23-8.8-31.8,0c-8.8,8.8-8.8,23,0,31.8c0,0,38.4,38.4,38.4,38.4 S88.4,50,88.4,50z"
   :stroke-file "M80,95H20V5h60V95z"
   :stroke-vertical "M50,95V5"
   :stroke-horizontal "M5,50h90"
   :stroke-requests "M95,65v30H65 c-16.6,0-30-13.4-30-30c0-16.6,13.4-30,30-30C81.6,35,95,48.4,95,65z M63.3,25C59.2,13.4,48.1,5,35,5C18.4,5,5,18.4,5,35v30h20 M65,80V50 M50,65h30"
   :stroke-chat-team "M35,35c16.6,0,30,13.4,30,30 c0,16.6-13.4,30-30,30H5V65C5,48.4,18.4,35,35,35z M75,65h20V35C95,18.4,81.6,5,65,5c-13.1,0-24.2,8.4-28.3,20"

   :fill-logomark "M43,100H29.5V39H43V100z M94,33.8C90.9,22,83.3,12.2,72.8,6.1C62.2,0,50-1.6,38.2,1.6 C26.5,4.7,16.6,12.3,10.6,22.8C4.5,33.3,2.9,45.6,6,57.4l1.7,6.4l12.7-3.4l-1.7-6.4c-4.6-17.2,5.6-35,22.9-39.6 c8.3-2.2,17.1-1.1,24.6,3.2c7.5,4.3,12.8,11.3,15.1,19.7c4.6,17.2-5.6,35-22.9,39.6L52,78.5l3.4,12.7l6.4-1.7 C86.1,83.1,100.5,58,94,33.8z"
   :fill-twitter "M100,19c-3.7,1.6-7.6,2.7-11.8,3.2c4.2-2.5,7.5-6.6,9-11.4c-4,2.4-8.4,4.1-13,5c-3.7-4-9.1-6.5-15-6.5 c-11.3,0-20.5,9.2-20.5,20.5c0,1.6,0.2,3.2,0.5,4.7c-17.1-0.9-32.2-9-42.3-21.4c-1.8,3-2.8,6.6-2.8,10.3c0,7.1,3.6,13.4,9.1,17.1 c-3.4-0.1-6.5-1-9.3-2.6c0,0.1,0,0.2,0,0.3c0,9.9,7.1,18.2,16.5,20.1c-1.7,0.5-3.5,0.7-5.4,0.7c-1.3,0-2.6-0.1-3.9-0.4 c2.6,8.2,10.2,14.1,19.2,14.2c-7,5.5-15.9,8.8-25.5,8.8c-1.7,0-3.3-0.1-4.9-0.3c9.1,5.8,19.9,9.2,31.4,9.2 c37.7,0,58.4-31.3,58.4-58.4c0-0.9,0-1.8-0.1-2.7C93.8,26.7,97.2,23.1,100,19z"
   :fill-google "M53.8,0C35.5,0,25.6,11.6,25.6,24.5c0,9.8,7.1,21,21.6,21h3.7c0,0-1,2.4-1,4.8c0,3.5,1.2,5.4,3.9,8.4 c-25,1.5-35.1,11.6-35.1,22.5c0,9.5,9.1,18.9,28.2,18.9c22.6,0,34.4-12.6,34.4-24.9c0-8.7-4.3-13.5-15.3-21.7 c-3.2-2.5-3.9-4.1-3.9-6c0-2.7,1.6-4.5,2.2-5.1c1-1.1,2.8-2.3,3.5-2.9c3.7-3.1,8.9-7.7,8.9-17c0-6.3-2.6-11.8-8.6-16.9h7.3L80.9,0 L53.8,0L53.8,0z M48.8,4.1c3.3,0,6.1,1.2,9,3.6c3.2,2.9,8.4,10.8,8.4,20.5c0,10.5-8.2,13.4-12.6,13.4c-2.2,0-4.8-0.6-6.9-2.1 C41.8,36.4,37,28,37,17.9C37,8.9,42.4,4.1,48.8,4.1z M56,62.7c1.4,0,2.4,0.1,2.4,0.1s3.3,2.4,5.6,4.1c5.4,4.2,8.7,7.5,8.7,13.2 c0,7.9-7.4,14.1-19.3,14.1c-13.1,0-23.1-6.1-23.1-16C30.4,70,37.2,62.9,56,62.7L56,62.7z"
   :fill-dribbble "M50,99.9C22.4,99.9,0,77.5,0,50C0,22.5,22.4,0.1,50,0.1 c27.6,0,50,22.4,50,49.9C100,77.5,77.6,99.9,50,99.9L50,99.9z M92.2,56.8c-1.5-0.5-13.2-4-26.6-1.8c5.6,15.3,7.9,27.8,8.3,30.4 C83.4,79,90.3,68.7,92.2,56.8L92.2,56.8z M66.7,89.3C66,85.6,63.6,72.5,57.6,57c-0.1,0-0.2,0.1-0.3,0.1 c-24.1,8.4-32.7,25.1-33.5,26.6c7.2,5.6,16.3,9,26.2,9C55.9,92.7,61.6,91.5,66.7,89.3L66.7,89.3z M18.3,78.6 c1-1.7,12.7-21,34.7-28.1c0.6-0.2,1.1-0.3,1.7-0.5c-1.1-2.4-2.2-4.8-3.5-7.2c-21.3,6.4-42,6.1-43.9,6.1c0,0.4,0,0.9,0,1.3 C7.3,61,11.5,71,18.3,78.6L18.3,78.6z M8.2,41.3c1.9,0,19.5,0.1,39.5-5.2C40.6,23.6,33,13,31.8,11.5C19.9,17.1,11,28.1,8.2,41.3 L8.2,41.3z M40,8.6c1.2,1.6,8.9,12.1,15.9,25c15.2-5.7,21.6-14.3,22.4-15.4C70.8,11.5,60.9,7.4,50,7.4C46.6,7.4,43.2,7.8,40,8.6 L40,8.6z M83.1,23.1c-0.9,1.2-8.1,10.4-23.8,16.8c1,2,1.9,4.1,2.8,6.2c0.3,0.7,0.6,1.5,0.9,2.2c14.2-1.8,28.3,1.1,29.7,1.4 C92.6,39.6,89,30.4,83.1,23.1L83.1,23.1z"
   :fill-github "M50,0C22.4,0,0,22.4,0,50c0,22.1,14.3,40.8,34.2,47.4 c2.5,0.5,3.4-1.1,3.4-2.4c0-1.2,0-4.3-0.1-8.5c-13.9,3-16.8-6.7-16.8-6.7c-2.3-5.8-5.6-7.3-5.6-7.3c-4.5-3.1,0.3-3,0.3-3 c5,0.4,7.7,5.2,7.7,5.2c4.5,7.6,11.7,5.4,14.6,4.2c0.5-3.2,1.7-5.4,3.2-6.7c-11.1-1.3-22.8-5.6-22.8-24.7c0-5.5,1.9-9.9,5.1-13.4 c-0.5-1.3-2.2-6.3,0.5-13.2c0,0,4.2-1.3,13.7,5.1c4-1.1,8.3-1.7,12.5-1.7c4.2,0,8.5,0.6,12.5,1.7c9.5-6.5,13.7-5.1,13.7-5.1 c2.7,6.9,1,12,0.5,13.2c3.2,3.5,5.1,8,5.1,13.4c0,19.2-11.7,23.4-22.8,24.7c1.8,1.5,3.4,4.6,3.4,9.3c0,6.7-0.1,12.1-0.1,13.7 c0,1.3,0.9,2.9,3.4,2.4C85.7,90.8,100,72.1,100,50C100,22.4,77.6,0,50,0z"
   :fill-slack "M99.7,52.8l-0.1-0.4c-1.2-3.5-4.5-5.8-8.3-5.7c-0.9,0-1.9,0.2-2.8,0.5l-9.8,3.3l-6.3-18.8l0,0l0,0 c9.3-3.2,9.4-3.2,9.4-3.2c3.9-1.3,7.2-5.5,5.9-10.1L87.6,18c-1.2-3.5-4.4-5.8-8.3-5.8c-0.9,0-1.7,0.1-2.6,0.4L67,15.8l-3.2-9.6 c-0.7-2.2-2.3-4-4.4-5c-1.2-0.6-2.6-0.9-3.9-0.9c-0.9,0-1.8,0.2-2.7,0.5c-4.4,1.5-6.9,6.2-5.6,10.6l0.1,0.4l3.2,9.6l-18.6,6.2 L28.7,18c-1.2-3.6-4.5-6-8.3-5.9c-0.9,0-1.8,0.2-2.7,0.5c-4.4,1.5-6.9,6.2-5.6,10.6c0,0.1,0.1,0.3,0.1,0.4l3.2,9.5l-9.4,3.2 c-4.4,1.5-6.9,6.1-5.6,10.6c0,0.1,0.1,0.3,0.1,0.4c1.2,3.5,4.3,5.7,8.1,5.7l0,0c1,0,1.9-0.2,2.9-0.5c3.3-1.1,6.4-2.2,9.4-3.2l0,0v0 l0,0l6.3,18.7l-9.4,3.2c-4.4,1.5-6.9,6.2-5.6,10.7c0,0.1,0.1,0.3,0.1,0.4c1.2,3.4,4.3,5.6,8.1,5.6v0c1,0,2-0.2,3-0.5l9.2-3.1 l3.3,9.7c1.2,3.6,4.5,6,8.3,5.9c0.9,0,1.8-0.2,2.7-0.5c4.4-1.5,6.9-6.2,5.6-10.6c0-0.1-0.1-0.3-0.1-0.4L49,78.4l18.6-6.4l3.3,9.9 c1.2,3.6,4.5,6,8.3,5.9l0,0c0.9,0,1.8-0.2,2.7-0.5c4.4-1.5,6.9-6.2,5.5-10.8l-0.1-0.2l-3.4-10l10.3-3.5c0,0,3.2-1.4,4.2-3 C100,57.8,100.4,55.1,99.7,52.8z M43.6,62.3l-6.3-18.7c7.3-2.5,13.5-4.6,18.6-6.3L62.2,56L43.6,62.3z"
   :fill-cognitect "M75.6,59.9L35.2,74.6V25.4l40.4,14.7V4.2c0-0.4-0.2-0.7-0.6-0.8l-9.1-3.3C65.7,0,65.4,0,65.1,0.2 c-0.2,0.2-0.4,0.4-0.4,0.7v23.7L25.6,10.3c-0.3-0.1-0.6-0.1-0.8,0.1c-0.2,0.2-0.4,0.4-0.4,0.7v77.7c0,0.3,0.1,0.5,0.4,0.7 c0.2,0.2,0.5,0.2,0.8,0.1l39.2-14.3v23.7c0,0.3,0.1,0.5,0.4,0.7c0.1,0.1,0.3,0.2,0.5,0.2c0.1,0,0.2,0,0.3-0.1l9.1-3.3 c0.3-0.1,0.6-0.5,0.6-0.8L75.6,59.9L75.6,59.9z"
   :fill-cognician "M67.3,39.6c0.1-0.2,0.8-0.9,1.6-0.9H69c0.5,0.1,0.8,0.2,1,0.7c0.1,0.1,0.1,0.5,0.1,0.6 c-0.2,0.4-0.4,0.8-1.1,1.2c-0.3,0.2-1.7,0.8-2.4,0.9l-0.4,0.1C66.5,41.3,66.9,40.3,67.3,39.6 M39.4,42.2c-0.7-0.1-2.2-0.8-2.4-0.9 c-0.7-0.5-0.9-0.9-1.1-1.2c-0.1-0.1,0-0.5,0.1-0.6c0.2-0.5,0.5-0.6,1-0.7H37c0.8,0,1.5,0.7,1.6,0.9c0.5,0.7,0.8,1.7,1.1,2.7 L39.4,42.2L39.4,42.2z M85.4,71.6C80.6,78.1,74.2,82,66.9,83.3c-1.2,0.2-2.9,0.6-4.6,0.2c-3.2-0.7-3.4-4.2-3.3-4.7l6.1-31.6 c0.5-0.1,0.9-0.2,1.3-0.3l1-0.2c1.5-0.3,3.6-1.3,4.3-1.8c1.5-1.2,2.2-2.3,2.6-3.1c0.8-1.8,0.3-3.6-0.1-4.5c-0.9-2-2.7-3.2-4.9-3.3 c-2.6-0.2-4.9,1.5-5.9,3c-1.1,1.8-1.8,4.2-2.2,6c-0.8,0.1-1.6,0.2-2.3,0.3c1.9-4.2,1-7-0.1-8.7c-1-1.6-3-3-5.7-2.9 c-2.8-0.1-4.7,1.3-5.7,2.9c-1.1,1.7-1.9,4.6-0.1,8.7c-0.8-0.1-1.6-0.2-2.3-0.3c-0.5-1.9-1.2-4.3-2.2-6c-1-1.6-3.2-3.2-5.9-3 c-2.2,0.1-3.9,1.3-4.9,3.3c-0.4,0.9-0.9,2.8-0.1,4.5c0.4,0.9,1,2,2.6,3.1c0.7,0.5,2.8,1.5,4.3,1.8l1,0.2c0.4,0.1,0.9,0.2,1.3,0.3 l6,32.5c0,2.1,1.2,4.8-5.9,6.8c-8.8,2.1-17.6,4.2-26.4,6.3c2.5-1.9,5-3.9,7.4-5.9c1.3-1.1,2.8-2.1,4.1-3.2c1.8-1.3,2.2-2.2,1.3-3.3 c-0.1-0.5-0.7-1.1-1.6-1.9c-4.6-3.5-8.3-7.7-11.1-12.5c-1.5-2.8-2.8-5.7-3.6-9C4.8,33.6,20.6,9.1,44.7,5.5 c18.2-2.7,35.5,6.1,43.7,21.4c1.8,3.5,3.2,7.3,4,11.4C94.4,50.2,92.8,61.6,85.4,71.6 M52.4,36.6c0.2-0.1,0.4-0.1,0.6-0.1 c0.2,0,0.4,0.1,0.6,0.1c0.6,0.2,1.8,0.9,1.5,3c0,0-0.3,1.4-1.6,2.5c-0.1,0.1-0.2,0.2-0.4,0.3c-0.2-0.1-0.4-0.2-0.4-0.3 c-1.3-1.1-1.6-2.5-1.6-2.5C50.6,37.5,51.9,36.8,52.4,36.6 M98.3,43.5c-0.5-6.8-2.2-13-5-18.4C89,16.4,81.8,9.5,72,4.7 c-13.2-6.4-26.7-6-40.2-0.2C6.5,15.4-2.4,44.3,9.2,66.6c2.5,5.2,6.1,9.9,10.9,14.1c0.3,0.2,0.6,0.5,0.8,0.8 c-2.8,2.2-5.5,4.3-8.1,6.4c-3.4,2.6-6.8,5.3-10.2,8c-0.9,0.7-1.3,1.5-0.8,2.9c0.8,1.9,3.4,1.3,3.6,1.2c9.5-2.3,19.1-4.6,28.6-6.9 c3.3-0.8,6.6-1.6,9.8-2.6c0,0,2.3-0.6,3.9-1.8c2.2-1.6,4.3-4.2,3.9-8.1l-6-32.4c1.5,0.2,13.1,0.2,14.5,0l-5.9,30.5 c-0.5,6.7,5.9,9.6,7.7,9.8l0.2,0.1c2.3,0.3,6.1-0.5,9.3-1.5c8.4-2.7,15.3-7.4,20.1-14.8C97.3,63.3,99.1,53.6,98.3,43.5"
   :fill-scroll-north "M35,27.5l15-15l15,15"
   :fill-scroll-south "M65,72.5l-15,15l-15-15H65z"
   :fill-click-cursor "M35,66.9V25l29.6,29.6h-12l6.9,16.6L50.3,75l-6.9-16.6L35,66.9z"
   :fill-dot "M57.5,50c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S57.5,45.9,57.5,50z"
   :fill-ellipsis "M59,50c0,5-4,9-9,9s-9-4-9-9s4-9,9-9S59,45,59,50z M14,41c-5,0-9,4-9,9s4,9,9,9s9-4,9-9S19,41,14,41z M86,41 c-5,0-9,4-9,9s4,9,9,9s9-4,9-9S91,41,86,41z"
   :fill-ellipsis-vertical "M50,41c5,0,9,4,9,9s-4,9-9,9s-9-4-9-9S45,41,50,41z M41,86c0,5,4,9,9,9s9-4,9-9s-4-9-9-9S41,81,41,86z M41,14c0,5,4,9,9,9s9-4,9-9s-4-9-9-9S41,9,41,14z"
   :fill-star "M50,2.4l15.5,31.3l34.5,5L75,63.1l5.9,34.4L50,81.3L19.1,97.6L25,63.1L0,38.8l34.6-5L50,2.4z"
   :fill-north "M30,20L50,0l20,20H30z"
   :fill-south "M70,80l-20,20L30,80H70z"
   :fill-east "M80,30l20,20L80,70V30z"
   :fill-west "M20,70L0,50l20-20V70z"
   :fill-up-down "M55,80H45V20h10V80z"
   :fill-left-right "M80,55H20V45h60V55z"
   :fill-lock-bottom "M87.5,95h-75V45h75V95z"
   :fill-read-only "M62.5,50c0,6.9-5.6,12.5-12.5,12.5S37.5,56.9,37.5,50 S43.1,37.5,50,37.5S62.5,43.1,62.5,50z"
   :fill-private "M80,95H20V50h60V95z"
   :fill-chat1 "M32.5,50c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S32.5,45.9,32.5,50z"
   :fill-chat2 "M82.5,50c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S82.5,45.9,82.5,50z"
   :fill-x1 "M32.5,25c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S32.5,20.9,32.5,25z"
   :fill-x2 "M82.5,25c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S82.5,20.9,82.5,25z"
   :fill-x3 "M45,37.5c0,4.1-3.4,7.5-7.5,7.5S30,41.6,30,37.5s3.4-7.5,7.5-7.5S45,33.4,45,37.5z"
   :fill-x4 "M70,37.5c0,4.1-3.4,7.5-7.5,7.5S55,41.6,55,37.5s3.4-7.5,7.5-7.5S70,33.4,70,37.5z"
   :fill-x5 "M57.5,50c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S57.5,45.9,57.5,50z"
   :fill-x6 "M45,62.5c0,4.1-3.4,7.5-7.5,7.5S30,66.6,30,62.5s3.4-7.5,7.5-7.5S45,58.4,45,62.5z"
   :fill-x7 "M70,62.5c0,4.1-3.4,7.5-7.5,7.5S55,66.6,55,62.5s3.4-7.5,7.5-7.5S70,58.4,70,62.5z"
   :fill-x8 "M32.5,75c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S32.5,70.9,32.5,75z"
   :fill-x9 "M82.5,75c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S82.5,70.9,82.5,75z"
   :fill-png "M100,76.1H50H30H0l25-25l15,15l25-25L100,76.1z M32.3,23.9c-4,0-7.3,3.3-7.3,7.3s3.3,7.3,7.3,7.3 s7.3-3.3,7.3-7.3S36.3,23.9,32.3,23.9z"
   :fill-pdf "M100,66.1c-0.4-5.2-9.2-8.6-9.2-8.6c-3.4-1.2-7.1-1.8-11.3-1.8c-4.5,0-9.3,0.7-15.6,2.1 c-5.5-3.9-10.3-8.8-13.9-14.3c-1.6-2.4-3-4.8-4.2-7.1c3-7.2,5.7-15,5.2-23.7C50.6,5.7,47.5,1,43.2,1c-2.9,0-5.5,2.2-7.5,6.5 C32,15.1,33,24.9,38.5,36.6c-2,4.7-3.9,9.6-5.7,14.3c-2.2,5.9-4.5,11.9-7.1,17.7C18.4,71.5,12.4,75,7.5,79.2 C4.2,82.1,0.2,86.4,0,90.8c-0.1,2.1,0.6,4,2.1,5.6C3.7,98.1,5.6,99,7.8,99c7.1,0,14-9.8,15.3-11.8c2.6-4,5.1-8.4,7.5-13.4 c6.1-2.2,12.5-3.8,18.8-5.4l2.2-0.6c1.7-0.4,3.4-0.9,5.2-1.4c1.9-0.5,3.9-1,5.8-1.6c6.4,4.1,13.3,6.7,20.1,7.7 c5.7,0.8,10.7,0.3,14.1-1.4C99.9,69.5,100.1,67.1,100,66.1z M17.6,83.6c-2.8,4.3-7.5,8.8-9.8,8.8c-0.2,0-0.5,0-0.9-0.5 c-0.3-0.3-0.3-0.5-0.3-0.8c0.1-1.6,2.2-4.4,5.2-7c2.7-2.4,5.8-4.4,9.3-6.3C19.9,79.9,18.8,81.8,17.6,83.6z M41.6,10.3 c0.6-1.3,1.2-2,1.5-2.4c0.5,0.8,1.2,2.6,1.4,5.2c0.3,5.1-0.8,10-2.4,14.8C40,22,39.1,15.5,41.6,10.3z M55.1,60.1 c-1.8,0.5-3.5,0.9-5.1,1.4L47.7,62c-4.5,1.1-9.1,2.3-13.6,3.7c1.7-4.1,3.3-8.3,4.9-12.4c1.2-3,2.3-6.1,3.6-9.2 c0.6,1,1.3,2.1,1.9,3.1c3.1,4.7,6.9,9,11.3,12.7C55.6,59.9,55.3,60,55.1,60.1z M83.6,66.1c-3.7-0.5-7.4-1.7-11.1-3.3 c6.6-1,11.6-0.7,16,0.9c1,0.4,2.7,1.3,3.9,2.2C90.2,66.5,87.1,66.6,83.6,66.1z"
   :fill-code "M41.6,82.8L34.3,80l24.1-62.8l7.4,2.8L41.6,82.8z M74.8,75.2l-5.6-5.6L88.8,50L69.2,30.4l5.6-5.6L100,50 L74.8,75.2z M25.2,75.2L0,50l25.2-25.2l5.6,5.6L11.2,50l19.6,19.6L25.2,75.2z"
   :fill-file-pdf "M72.5,57.2c-0.2-2.4-4.1-3.9-4.2-3.9c-1.5-0.5-3.2-0.8-5.1-0.8c-2,0-4.2,0.3-7,0.9c-2.5-1.8-4.6-4-6.3-6.4 c-0.7-1.1-1.3-2.2-1.9-3.2c1.4-3.2,2.6-6.7,2.4-10.7c-0.2-3.1-1.6-5.2-3.5-5.2c-1.3,0-2.5,1-3.4,2.9c-1.6,3.4-1.2,7.8,1.3,13.1 c-0.9,2.1-1.7,4.3-2.5,6.4c-1,2.6-2,5.4-3.2,8c-3.3,1.3-6,2.9-8.2,4.8c-1.5,1.3-3.2,3.2-3.3,5.2c0,1,0.3,1.8,0.9,2.5 C29.1,71.6,30,72,31,72c3.2,0,6.3-4.4,6.9-5.3c1.2-1.8,2.3-3.8,3.4-6c2.7-1,5.6-1.7,8.4-2.4l1-0.3c0.8-0.2,1.5-0.4,2.4-0.6 c0.9-0.2,1.7-0.5,2.6-0.7c2.9,1.8,6,3,9,3.5c2.6,0.4,4.8,0.2,6.4-0.6C72.5,58.8,72.5,57.7,72.5,57.2z M35.4,65.1 c-1.3,1.9-3.4,4-4.4,4c-0.1,0-0.2,0-0.4-0.2c-0.1-0.1-0.1-0.2-0.1-0.3c0-0.7,1-2,2.3-3.1c1.2-1.1,2.6-2,4.2-2.8 C36.5,63.5,36,64.3,35.4,65.1z M46.2,32.2c0.3-0.6,0.5-0.9,0.7-1.1c0.2,0.4,0.5,1.2,0.6,2.3c0.1,2.3-0.4,4.5-1.1,6.6 C45.5,37.4,45.1,34.5,46.2,32.2z M52.3,54.5c-0.8,0.2-1.6,0.4-2.3,0.6l-1,0.3c-2,0.5-4.1,1-6.1,1.7c0.8-1.9,1.5-3.8,2.2-5.6 c0.5-1.4,1.1-2.8,1.6-4.1c0.3,0.5,0.6,0.9,0.9,1.4c1.4,2.1,3.1,4.1,5.1,5.7C52.5,54.5,52.4,54.5,52.3,54.5z M65.1,57.2 c-1.6-0.2-3.3-0.7-5-1.5c2.9-0.4,5.2-0.3,7.2,0.4c0.5,0.2,1.2,0.6,1.7,1C68.1,57.4,66.7,57.5,65.1,57.2z"
   :fill-dn-logo "M38.7,42.1c1.1,1.8,1.6,4.5,1.6,7.9c0,2.5-0.4,4.6-1.1,6.4c-1.3,3.3-3.6,4.9-6.9,4.9h-6.5V39.3h6.5 C35.5,39.3,37.6,40.2,38.7,42.1z M100,50c0,27.6-22.4,50-50,50S0,77.6,0,50S22.4,0,50,0S100,22.4,100,50z M47.4,49.5 c0-1.8-0.2-3.6-0.6-5.6c-0.4-1.9-1.2-3.8-2.4-5.4c-1.5-2.1-3.4-3.5-5.7-4.3c-1.4-0.5-3.1-0.7-5.2-0.7H19v33.6h14.5 c5.1,0,8.8-2.1,11.3-6.3C46.5,57.7,47.4,53.9,47.4,49.5z M79.8,33.5h-6.5v23.4L59.9,33.5h-7.4v33.6h6.5V43.3l13.7,23.8h7V33.5z"
   })

(def icon-templates
  {
   :precursor   {:paths [:stroke-precursor]}
   :download    {:paths [:stroke-download]}
   :check       {:paths [:stroke-check]}
   :times       {:paths [:stroke-times]}
   :cursor      {:paths [:stroke-cursor]}
   :rectangle   {:paths [:stroke-rectangle]}
   :line        {:paths [:stroke-line]}
   :text        {:paths [:stroke-text]}
   :pencil      {:paths [:stroke-pencil]}
   :ellipse     {:paths [:stroke-ellipse]}
   :mic         {:paths [:stroke-mic]}
   :mic-off     {:paths [:stroke-mic
                         :stroke-slash-backward]}
   :user        {:paths [:stroke-user]}
   :users       {:paths [:stroke-users]}
   :bullet      {:paths [:stroke-dot]}
   :chats       {:paths [:stroke-chats]}
   :crosshair   {:paths [:stroke-crosshair]}
   :ibeam       {:paths [:stroke-ibeam]}
   :blog        {:paths [:stroke-blog]}
   :clock       {:paths [:stroke-clock]}
   :delete      {:paths [:stroke-delete]}
   :shift       {:paths [:stroke-shift]}
   :option      {:paths [:stroke-option]}
   :control     {:paths [:stroke-control]}
   :command     {:paths [:stroke-command]}
   :esc         {:paths [:stroke-esc]}
   :space       {:paths [:stroke-space]}
   :mouse       {:paths [:stroke-mouse]}
   :globe       {:paths [:stroke-globe]}
   :doc         {:paths [:stroke-doc]}
   :docs        {:paths [:stroke-docs]}
   :docs-team   {:paths [:stroke-docs-team]}
   :newdoc      {:paths [:stroke-newdoc]}
   :login       {:paths [:stroke-login]}
   :logout      {:paths [:stroke-logout]}
   :info        {:paths [:stroke-info]}
   :home        {:paths [:stroke-home]}
   :hash        {:paths [:stroke-hash]}
   :arrow-up    {:paths [:stroke-arrow-up]}
   :arrow-down  {:paths [:stroke-arrow-down]}
   :arrow-left  {:paths [:stroke-arrow-left]}
   :arrow-right {:paths [:stroke-arrow-right]}
   :at          {:paths [:stroke-at]}
   :sharing     {:paths [:stroke-sharing]}
   :email       {:paths [:stroke-email]}
   :phone       {:paths [:stroke-phone]}
   :activity    {:paths [:stroke-activity]}
   :credit      {:paths [:stroke-credit]}
   :heart       {:paths [:stroke-heart]}
   :public      {:paths [:stroke-public]}
   :team        {:paths [:stroke-team]}
   :requests    {:paths [:stroke-requests]}
   :star        {:paths [:stroke-star]}
   :loading     {:paths [:stroke-border-circle]}
   :minus       {:paths [:stroke-horizontal]}
   :plus        {:paths [:stroke-horizontal
                         :stroke-vertical]}
   :clips       {:paths [:stroke-border-square
                         :stroke-horizontal
                         :stroke-vertical]}
   :read-only   {:paths [:stroke-read-only
                         :fill-read-only]}
   :settings    {:paths [:stroke-dot
                         :stroke-cog]}
   :menu        {:paths [:stroke-menu-top
                         :stroke-menu-mid
                         :stroke-menu-btm]}
   :sound       {:paths [:stroke-sound
                         :stroke-wave1
                         :stroke-wave2
                         :stroke-wave3]}

   :lock        {:paths [:stroke-private
                         :stroke-private-bottom]}
   :private     {:paths [:stroke-private
                         :fill-private]}
   :chat        {:paths [:stroke-chat
                         :fill-chat1
                         :fill-x5
                         :fill-chat2]}
   :chat-team   {:paths [:stroke-chat-team]}
   :feedback    {:paths [:stroke-feedback]}

   :sound-off   {:paths [:stroke-sound]}
   :sound-min   {:paths [:stroke-sound
                         :stroke-wave1]}
   :sound-med   {:paths [:stroke-sound
                         :stroke-wave1
                         :stroke-wave2]}
   :sound-max   {:paths [:stroke-sound
                         :stroke-wave1
                         :stroke-wave2
                         :stroke-wave3]}
   :sound-mute  {:paths [:stroke-sound
                         :stroke-wave1
                         :stroke-wave2
                         :stroke-wave3
                         :stroke-slash-backward]}

   :click       {:paths [:fill-click-cursor
                         :stroke-click-1
                         :stroke-click-2
                         :stroke-click-3]}
   :chat-morph  {:paths [:stroke-chat
                         :fill-x1
                         :fill-x2
                         :fill-x3
                         :fill-x4
                         :fill-x5
                         :fill-x6
                         :fill-x7
                         :fill-x8
                         :fill-x9]}
   :logomark    {:paths [:fill-logomark]}
   :twitter     {:paths [:fill-twitter]}
   :google-logo {:paths [:fill-google]}
   :dribbble    {:paths [:fill-dribbble]}
   :github      {:paths [:fill-github]}
   :slack       {:paths [:fill-slack]}
   :cognitect   {:paths [:fill-cognitect]}
   :cognician   {:paths [:fill-cognician]}
   :ph-logo     {:paths [:fill-ph-logo]}
   :dn-logo     {:paths [:fill-dn-logo]}
   :file-png    {:paths [:fill-png]}
   :file-pdf    {:paths [:fill-pdf]}
   :file-svg    {:paths [:fill-code]}
   :ellipsis    {:paths [:fill-ellipsis]}
   :dot-menu    {:paths [:fill-ellipsis-vertical]}
   :starred     {:paths [:fill-star]}
   :scroll      {:paths [:fill-scroll-north
                         :fill-dot
                         :fill-scroll-south]}
   :north       {:paths [:fill-up-down
                         :fill-north]}
   :south       {:paths [:fill-up-down
                         :fill-south]}
   :east        {:paths [:fill-left-right
                         :fill-east]}
   :west        {:paths [:fill-left-right
                         :fill-west]}
   :north-south {:paths [:fill-up-down
                         :fill-north
                         :fill-south]}
   :east-west   {:paths [:fill-left-right
                         :fill-east
                         :fill-west]}
   })

(defn svg-icon [icon-name & [{:keys [path-props svg-props]}]]
  (let [template (get icon-templates icon-name)]
    (apply dom/svg (clj->js (merge {:viewBox "0 0 100 100" :className (str "iconpile " (:className svg-props))}
                                   (dissoc svg-props :className)))
           (for [path (:paths template)]
             (dom/path (clj->js (merge {:className (str (name path) " " (:className path-props))
                                        :d (get icon-paths path)
                                        :key (name path)
                                        :vectorEffect "non-scaling-stroke"}
                                       (dissoc path-props :className))))))))

(defn icon [icon-name & [{:keys [path-props svg-props]}]]
  (dom/i #js {:className (str "icon-" (name icon-name))}
         (svg-icon icon-name {:path-props path-props
                              :svg-props svg-props})))

(def spinner
  (icon :logomark-precursor))

(defn mixpanel-badge []
  [:a#vendor.mixpanel-badge
   {:href "https://mixpanel.com/f/partner"
    :on-click #(.stopPropagation %)
    :alt "Mobile Analytics"
    :target "_blank"}
   [:svg {:width "114" :height "36"}
    [:path {:d "M39.2,13c0,1.1-0.8,2.1-2,2.1c-1.3,0-2-0.9-2-2.1c0-1.2,0.8-2.1,2-2.1C38.4,10.9,39.2,11.7,39.2,13z M72.5,15.4c1.2,0,2-0.9,2-2.2c0-1.2-0.8-2.2-2-2.2c-1.2,0-2,0.9-2,2.2C70.6,14.5,71.3,15.4,72.5,15.4z M76.1,24h2.2l-1.1-2.9 L76.1,24z M51.7,10.8c-1,0-1.7,0.7-1.8,1.6h3.5C53.4,11.5,52.7,10.8,51.7,10.8z M63.5,24h2.2l-1.1-2.9L63.5,24z M80.3,14.5 c0-0.5-0.3-0.8-0.9-0.8h-1.7v1.7h1.7C80,15.3,80.3,15,80.3,14.5z M30.1,10.8c-1.2,0-1.9,1-1.9,2.2c0,1.2,0.8,2.2,1.9,2.2 c1.2,0,1.9-0.9,1.9-2.2C32.1,11.7,31.3,10.8,30.1,10.8z M114,0v36H0V0H114z M84.7,16.2h3.7v-0.9h-2.6v-5.1h-1.1V16.2z M82.5,16.2 h1.1v-6h-1.1V16.2z M76.6,16.2h3c1.1,0,1.7-0.7,1.7-1.6c0-0.7-0.5-1.4-1.2-1.5c0.6-0.1,1-0.6,1-1.4c0-0.8-0.6-1.5-1.7-1.5h-3V16.2z M69.5,13.2c0,1.8,1.3,3.1,3.1,3.1c1.8,0,3.1-1.3,3.1-3.1c0-1.8-1.3-3.1-3.1-3.1C70.7,10.1,69.5,11.5,69.5,13.2z M62.2,16.2h1.1 v-4.6l1.8,4.6h0.5l1.8-4.6v4.6h1.1v-6H67l-1.6,4.1l-1.6-4.1h-1.5V16.2z M10.6,23.8c0-1.2-1-2.2-2.2-2.2s-2.2,1-2.2,2.2 c0,1.2,1,2.2,2.2,2.2S10.6,25.1,10.6,23.8z M13.7,10.9c1.1,0,1.5,0.8,1.5,1.8v2.9c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7v-3 c0-1.7-0.8-2.9-2.6-2.9c-1,0-1.8,0.4-2.4,1.3c-0.4-0.8-1.2-1.3-2.2-1.3c-0.9,0-1.6,0.4-2,1.2v-0.5c0-0.4-0.2-0.7-0.7-0.7 C6.3,9.8,6,10.1,6,10.5v5.1c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7v-2.9c0-1,0.6-1.8,1.7-1.8c1.1,0,1.6,0.8,1.6,1.8v2.9 c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7v-2.9C12,11.8,12.6,10.9,13.7,10.9z M17.9,23.9c0-0.9-0.7-1.6-1.6-1.6 c-0.9,0-1.6,0.7-1.6,1.6c0,0.9,0.7,1.6,1.6,1.6C17.2,25.4,17.9,24.7,17.9,23.9z M19.2,10.4c0-0.4-0.3-0.7-0.7-0.7 c-0.4,0-0.7,0.3-0.7,0.7v5.1c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7V10.4z M19.3,8c0-0.4-0.4-0.8-0.8-0.8 c-0.4,0-0.8,0.4-0.8,0.8c0,0.4,0.4,0.8,0.8,0.8C18.9,8.8,19.3,8.4,19.3,8z M23.6,23.9c0-0.4-0.3-0.7-0.7-0.7s-0.7,0.3-0.7,0.7 c0,0.4,0.3,0.7,0.7,0.7S23.6,24.3,23.6,23.9z M25.8,15.7c0-0.2-0.1-0.3-0.2-0.4L23.7,13l1.9-2.3c0.1-0.1,0.2-0.3,0.2-0.4 c0-0.3-0.2-0.6-0.6-0.6c-0.2,0-0.3,0.1-0.4,0.2L23,12.1L21.3,10c-0.1-0.1-0.3-0.2-0.4-0.2c-0.4,0-0.6,0.2-0.6,0.6 c0,0.1,0,0.3,0.2,0.4l1.9,2.3l-1.9,2.3c-0.1,0.1-0.2,0.2-0.2,0.4c0,0.3,0.3,0.6,0.6,0.6c0.2,0,0.4-0.1,0.5-0.2l1.7-2.1l1.7,2.1 c0.1,0.1,0.3,0.2,0.5,0.2C25.5,16.3,25.8,16,25.8,15.7z M33.4,13c0-1.9-1.4-3.3-3-3.3c-1,0-1.8,0.5-2.3,1.2v-0.5 c0-0.4-0.2-0.7-0.7-0.7s-0.6,0.3-0.6,0.7v7.8c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7v-3.2c0.5,0.7,1.3,1.2,2.2,1.2 C32.1,16.3,33.4,15,33.4,13z M40.6,13c0-1.9-1.2-3.3-3.3-3.3c-2,0-3.3,1.5-3.3,3.3c0,1.8,1.2,3.3,3.2,3.3c0.9,0,1.7-0.4,2.1-1.1v0.3 c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7h0V13z M47.7,12.7c0-1.7-1.3-3-3-3c-1.7,0-3,1.3-3,3v2.9c0,0.4,0.3,0.7,0.7,0.7 c0.4,0,0.7-0.3,0.7-0.7v-3c0-0.9,0.7-1.6,1.7-1.6c1,0,1.7,0.7,1.7,1.6v3c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7V12.7z M54.7,13c0-1.8-1.2-3.3-3-3.3c-1.9,0-3.2,1.4-3.2,3.3c0,1.8,1.1,3.3,3.3,3.3c1,0,2-0.4,2.6-1.1c0.1-0.1,0.2-0.2,0.2-0.4 c0-0.3-0.3-0.6-0.6-0.6c-0.2,0-0.3,0.1-0.4,0.2c-0.5,0.4-0.9,0.7-1.7,0.7c-1.2,0-1.9-0.8-1.9-1.7h4.3C54.6,13.5,54.7,13.3,54.7,13z M57,7.1c0-0.4-0.3-0.7-0.7-0.7c-0.4,0-0.7,0.3-0.7,0.7v8.5c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7V7.1z M67.6,26.1l-2.4-6H64 l-2.4,6h1.2l0.4-1.2H66l0.4,1.2H67.6z M73.5,20.1h-1.1v4.2l-3.1-4.2h-1.1v6h1.1v-4.3l3.1,4.3h1V20.1z M80.2,26.1l-2.4-6h-1.3l-2.4,6 h1.2l0.4-1.2h2.8l0.4,1.2H80.2z M84.4,25.1h-2.6v-5.1h-1.1v6h3.7V25.1z M89.3,20.1h-1.2l-1.6,2.6l-1.6-2.6h-1.2l2.3,3.5v2.5H87v-2.5 L89.3,20.1z M89.3,16.2h4.1v-0.9h-3.1v-1.7h3v-0.9h-3v-1.5h3.1v-0.9h-4.1V16.2z M94.3,20.1h-4.7V21h1.8v5.1h1.1V21h1.8V20.1z M96.2,20.1h-1.1v6h1.1V20.1z M102.8,24.8l-0.9-0.5c-0.3,0.5-0.9,0.9-1.5,0.9c-1.2,0-2.1-0.9-2.1-2.2c0-1.3,0.9-2.2,2.1-2.2 c0.6,0,1.2,0.4,1.5,0.9l0.9-0.5c-0.4-0.7-1.2-1.3-2.4-1.3c-1.8,0-3.2,1.3-3.2,3.1c0,1.8,1.4,3.1,3.2,3.1 C101.6,26.2,102.3,25.5,102.8,24.8z M108,24.3c0-2.2-3.5-1.5-3.5-2.7c0-0.4,0.4-0.7,1-0.7c0.6,0,1.3,0.2,1.7,0.7l0.6-0.8 c-0.5-0.5-1.3-0.8-2.2-0.8c-1.3,0-2.2,0.8-2.2,1.8c0,2.2,3.5,1.4,3.5,2.7c0,0.4-0.3,0.8-1.2,0.8c-0.8,0-1.5-0.4-1.9-0.8l-0.6,0.8 c0.5,0.6,1.3,1,2.4,1C107.3,26.2,108,25.3,108,24.3z M80.2,11.9c0-0.4-0.3-0.8-0.8-0.8h-1.7v1.5h1.7C79.9,12.7,80.2,12.4,80.2,11.9z"}]]])

(defn google-login [{:keys [size source]} owner]
  (reify
    om/IDisplayName (display-name [_] "Google login")
    om/IRender
    (render [_]
      (let [cast! (om/get-shared owner :cast!)]
        (html
         [:a#vendor.google-login
          {:class (when (= :small size) "small")
           :role "button"
           :data-after "Sign in"
           :target "_self"
           :href (auth/auth-url :source source)}
          (icon :google-logo)
          [:div.google-text "Sign in with Google"]])))))

(defn volume-icon-kw [level]
  (cond (= 0 level) :sound-off
        (>= 10 level) :sound-min
        (>= 50 level) :sound-med
        :else :sound-max))
