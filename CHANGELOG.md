# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The changes documented here do not include those from the original repository.


## [Unreleased]

## 12-08-2022
Android - Fix on retrieving sleep data (https://outsystemsrd.atlassian.net/browse/RMET-1734)

## [Version 1.2.7]

- Fix: Apply Date Range filter while processing Advanced Query Results, so that data blocks that don't belong to the requested dates can be omitted (https://outsystemsrd.atlassian.net/browse/RMET-1718).
- Fix: Create AdvancedQueryDataPoint structure and AdvancedQueryResultType parameter so that we can control what to output on the native side (https://outsystemsrd.atlassian.net/browse/RMET-1724).

## [Version 1.2.6]

- Feat: Implemented option to return only filled blocks in the advanced query. (https://outsystemsrd.atlassian.net/browse/RMET-1714)

## [Version 1.2.5]

- Removed hook that adds swift support and added the plugin as dependecy. (https://outsystemsrd.atlassian.net/browse/RMET-1680)
- Added to the info.plist file a property to identify the background tasks used in the plugin.(https://outsystemsrd.atlassian.net/browse/RMET-1689) 

## [Version 1.2.4]

- Assign the "averageOperations" value for Blood Glucose's optionsAllowed property (https://outsystemsrd.atlassian.net/browse/RPM-2623)

## [Version 1.2.3]

- Implemented query for category types (https://outsystemsrd.atlassian.net/browse/RMET-1507)

## [Version 1.2.2]

- Fixes problem with Kotlin versions (https://outsystemsrd.atlassian.net/browse/RMET-1438)

- Fixed issue with MABS 7 Android build because of PendingIntent.FLAG_MUTABLE (https://outsystemsrd.atlassian.net/browse/RMET-1460)

## [Version 1.2.1]

- Fixes Background Jobs bugs on Android 12 due to missing PendingIntent flag


## [Version 1.2.0]

- Implemented Unit Tests for BackgroundJob operations on iOS (https://outsystemsrd.atlassian.net/browse/RMET-1210)

- Fixed ClickActivity package name on plugin.xml (https://outsystemsrd.atlassian.net/browse/RMET-1268)

- Implemented Unit Tests for UpdateBackgroundJob feature on Android (https://outsystemsrd.atlassian.net/browse/RMET-1235)

- Implemented Unit Tests for DeleteBackgroundJob feature on Android (https://outsystemsrd.atlassian.net/browse/RMET-1232)

- Implemented Unit Tests for ListBackgroundJob feature on Android (https://outsystemsrd.atlassian.net/browse/RMET-1239)

- Implementation of UpdateBackgroundJob for Android (https://outsystemsrd.atlassian.net/browse/RMET-1235)

- Implementation of DeleteBackgroundJob for Android (https://outsystemsrd.atlassian.net/browse/RMET-1232)

- Implementation of ListBackgroundJobs for Android (https://outsystemsrd.atlassian.net/browse/RMET-1239)

- Implementation of notificaiton frequency (waiting period) for Android (https://outsystemsrd.atlassian.net/browse/RMET-1240)

## [Version 1.1.0]

## 2021-11-18
- Implementation of new variables and new version of background job iOS (https://outsystemsrd.atlassian.net/browse/RMET-1138)

## 2021-11-12
- Implementation of the background job iOS (https://outsystemsrd.atlassian.net/browse/RMET-1133)

- Test: Added setBackgroundJob unit tests for Android (https://outsystemsrd.atlassian.net/browse/RMET-1191)

- Implementation of the background job (https://outsystemsrd.atlassian.net/browse/RMET-1133)

## 2021-11-11
- Implemented SetBackgroundJob action with notifications (https://outsystemsrd.atlassian.net/browse/RMET-1070)

## 2021-11-04
- Implemented setup phase of SetBackgroundJob action (https://outsystemsrd.atlassian.net/browse/RMET-1130)

## [Version 1.0.3]
- Introducing WALKING_SPEED and DISTANCE variables

## [Version 1.0.2]
- Updated Health and Fitness variables list and general improvements

## [Version 1.0.1]

## 2021-11-05
- Created new release for updates in wrapper Extensability Configurations

## 2021-11-02
- Feat: Added setBackgroundJob feature (https://outsystemsrd.atlassian.net/browse/RMET-1130)

## 2021-10-20
- Fixed compatibility issue with MABS 7 (https://outsystemsrd.atlassian.net/browse/RMET-1168)

## 2021-10-18
- Refactored method to check for Google Play Services (https://outsystemsrd.atlassian.net/browse/RMET-1153)

## 2021-10-14
- Unit Tests for iOS (https://outsystemsrd.atlassian.net/browse/RMET-1049)

## 2021-10-14
- Unit Tests for Android (https://outsystemsrd.atlassian.net/browse/RMET-1048)

## [Version 1.0.0]

## 2021-10-04
- Refactorings for Android (https://outsystemsrd.atlassian.net/browse/RMET-1066)

## 2021-09-21
- Fixed requestPermissions callback for Android (https://outsystemsrd.atlassian.net/browse/RMET-996)

## 2021-09-21
- Using getLastSignedInAccount for Android (https://outsystemsrd.atlassian.net/browse/RMET-1022)

## 2021-09-15
- Implemented getLastRecord for Android (https://outsystemsrd.atlassian.net/browse/RMET-831)

## 2021-09-15
- Implemented WriteProfileData for Android (https://outsystemsrd.atlassian.net/browse/RMET-976)

## 2021-09-13
- Implemeted method to write profile data (https://outsystemsrd.atlassian.net/browse/RMET-995)

## 2021-09-13
- Fixed RequestPermissions for Android (https://outsystemsrd.atlassian.net/browse/RMET-971)

## 2021-09-10
- Added code to check for google play services (https://outsystemsrd.atlassian.net/browse/RMET-830)

## 2021-09-10
- Implemented RequestPermissions for Android (https://outsystemsrd.atlassian.net/browse/RMET-971)
