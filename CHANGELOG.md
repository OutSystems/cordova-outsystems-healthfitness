# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The changes documented here do not include those from the original repository.

## [2.2.1]

### Fixes

- (android): Replace removeLast with compatible List method (https://outsystemsrd.atlassian.net/browse/RMET-3593).

## [2.2.0]

### Features

- (ios): Implement support for `Body Temperature` health variable (https://outsystemsrd.atlassian.net/browse/RMET-3672).
- (android): Implement support for `Body Temperature` health variable (https://outsystemsrd.atlassian.net/browse/RMET-3673).

## [2.1.2]

- Fix: Request `READ_HEALTH_DATA_IN_BACKGROUND` permission for Android 15 when setting a background job (https://outsystemsrd.atlassian.net/browse/RMET-3574).

## [2.1.1]

### 2024-05-22
- Fix: Fixes incompatibility with AppShield plugin by updating dependency to Android native library (https://outsystemsrd.atlassian.net/browse/RMET-3393).

## [2.1.0]

### 2024-05-09
- Fix: Add format verification for Android's Privacy Policy file (https://outsystemsrd.atlassian.net/browse/RMET-3406).
- Feat: Implemented support for `OxygenSaturation` variable when using requestPermissions, advancedQuery, getHealthData, and background jobs (https://outsystemsrd.atlassian.net/browse/RMET-3363).

### 2024-05-09
- Fix: Use proper unit for percentage values (https://outsystemsrd.atlassian.net/browse/RMET-3414).

### 2024-04-24
- Chore: Update cordova hooks with new OutSystems specific errors. (https://outsystemsrd.atlassian.net/browse/RMET-3388).

## [2.0.0]

### 2024-04-05
- Fix privacy policy opening for Android <= 13 (https://outsystemsrd.atlassian.net/browse/RMET-3134).

### 2024-04-04
- Update the iOS framework. This adds the Privacy Manifest file (https://outsystemsrd.atlassian.net/browse/RMET-3280).
- Update GSON version to remove vulnerability (https://outsystemsrd.atlassian.net/browse/RMET-3134).

### 2024-04-01
- Remove old code and re-arrange file structure (https://outsystemsrd.atlassian.net/browse/RMET-3134).

### 2024-03-22
- Fixed hook for ODC  (https://outsystemsrd.atlassian.net/browse/RMET-3191).

### 2024-03-18
- Implemented the usage of the Activity Transition Recognition API for background jobs  (https://outsystemsrd.atlassian.net/browse/RMET-3246).

### 2024-03-14
- Implemented the usage of exact alarms for background jobs  (https://outsystemsrd.atlassian.net/browse/RMET-3190).

### 2024-02-28
- Implemented `Open Health Connect App`  (https://outsystemsrd.atlassian.net/browse/RMET-3158).

### 2024-02-27
- Implemented hook for permissions  (https://outsystemsrd.atlassian.net/browse/RMET-3142).

### 2024-02-26
- Implemented `Show app's privacy policy dialog`  (https://outsystemsrd.atlassian.net/browse/RMET-3145).

### 2024-02-23
- Re-implement `DeleteBackgroundJob` feature (https://outsystemsrd.atlassian.net/browse/RMET-3068).

### 2024-02-20
- Deprecated `DisableGoogleFit` feature and implemented `DisableHealthConnect` feature (https://outsystemsrd.atlassian.net/browse/RMET-3070).
- Re-implemented UpdateBackgroundJob feature (https://outsystemsrd.atlassian.net/browse/RMET-3067).
- Re-implemented SetBackgroundJob feature (https://outsystemsrd.atlassian.net/browse/RMET-3050).

### 2024-02-09
- Re-implemented AdvanceQuery feature (https://outsystemsrd.atlassian.net/browse/RMET-3047).

### 2024-02-09
- Re-implemented ListBackgroundJobs feature (https://outsystemsrd.atlassian.net/browse/RMET-3069).

### 2024-02-08
- Re-implement `GetLastRecord` feature:
    - GetFitnessData (https://outsystemsrd.atlassian.net/browse/RMET-3048)
    - GetHealthData (https://outsystemsrd.atlassian.net/browse/RMET-3065)
    - GetProfileData (https://outsystemsrd.atlassian.net/browse/RMET-3066)

### 2024-02-05
- Re-implemented WriteProfieleData feature (https://outsystemsrd.atlassian.net/browse/RMET-3049).

### 2024-02-01
- Re-implemented RequestPermissions feature (https://outsystemsrd.atlassian.net/browse/RMET-3046).

## [Version 1.4.0]

## 2023-08-25
- Implemented feature to disable Google Fit (https://outsystemsrd.atlassian.net/browse/RMET-2723).

## [Version 1.3.0]
- Feat: [iOS] Add a method to retrieve workouts raw data from HealthKit (https://outsystemsrd.atlassian.net/browse/RMET-2128).

## [Version 1.2.12]

### 16-12-2022
- Replaced jcenter with more up to date mavenCentral [RMET-2036](https://outsystemsrd.atlassian.net/browse/RMET-2036)
## [Version 1.3.0]
- Feat: [iOS] Add a method to retrieve workouts raw data from HealthKit (https://outsystemsrd.atlassian.net/browse/RMET-2128).

## [Version 1.2.11]
- Fix: [iOS] Replace the old `OSCore` framework for the new `OSCommonPluginLib` pod.

## [Version 1.2.10]
- Fix: [Android] Add safe call to avoid build error. (https://outsystemsrd.atlassian.net/browse/RMET-2041)
- Fix: [iOS] iOS 13 "Variable not Available" Issue. (https://outsystemsrd.atlassian.net/browse/RMET-1958)

## [Version 1.2.9]
- Fix: [iOS] Clear milliseconds from background job's date range lower bound, as `HKStatistics` uses for its start date. This way, the data block is not discarded from the final results (https://outsystemsrd.atlassian.net/browse/RMET-1836).
- Feat: [Android] Implement request permissions, feature needed to android 13 compliance.
- Fix: [iOS] Apply the AdvancedQueryDataPoint structure and AdvancedQueryResultType parameter to Category and Correlation variables as well.
- Fix: [iOS] Fix getLastRecord's result by fetching only the latest filled value.

## [Version 1.2.8]
- Fix: [Android] Fix on retrieving sleep data (https://outsystemsrd.atlassian.net/browse/RMET-1734)

## [Version 1.2.7]

- Fix: [iOS] Apply Date Range filter while processing Advanced Query Results, so that data blocks that don't belong to the requested dates can be omitted (https://outsystemsrd.atlassian.net/browse/RMET-1718).
- Fix: [iOS] Create AdvancedQueryDataPoint structure and AdvancedQueryResultType parameter so that we can control what to output on the native side (https://outsystemsrd.atlassian.net/browse/RMET-1724).

## [Version 1.2.6]

- Feat: [iOS] Implemented option to return only filled blocks in the advanced query. (https://outsystemsrd.atlassian.net/browse/RMET-1714)

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
