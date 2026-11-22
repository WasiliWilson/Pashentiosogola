# SOFTWARE TEST CASE SPECIFICATION
## PROJECT: WODWALA OTSOGOLA (Smart Medication Reminder & Adherence System)

---

### DOCUMENT CONTROL INFORMATION
- **Project Name:** Wodwala Otsogola
- **Document Type:** Comprehensive Test Case Specification Template
- **Author / QA Engineer:** Blessings Chibwana Template Standard
- **Target Platform:** Android (Jetpack Compose, Material 3, Persistent JSON Database, AlarmManager)
- **Status:** Verified & Approved

---

## 1. INTRODUCTION & SCOPE
Wodwala Otsogola is an advanced Android application engineered to help patients manage their medication schedules, receive automated alarms and full-screen popup notifications when medicine is due, track remaining pill supply, log daily health journal notes, and maintain 100% medication adherence. This document specifies the comprehensive test cases executed to validate system functionality, data persistence, alarm reliability, and user interface accessibility.

---

## 2. TEST ENVIRONMENT & PREREQUISITES
- **Hardware:** Android Smartphone / Emulator (API 24 to 37)
- **Software:** Android Studio, Jetpack Compose M3, Local JSON Storage Repository
- **Test Tools:** JUnit, Compose UI Test Runner, Android Logcat, Visual Lint / Accessibility Checker

---

## 3. TEST CASE EXECUTION TABLE

| Test Case ID | Module / Feature | Test Objective / Description | Pre-conditions | Test Steps | Test Data | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_WO_01** | UI & Navigation | Verify app launches and bottom navigation works across all screens | App installed & opened | 1. Open app<br>2. Tap Home, Meds, Refills, History, Settings | Default state | All screens load correctly and bottom bar switches tabs | All screens loaded successfully | **PASS** |
| **TC_WO_02** | Medication Management | Verify adding a new medication with schedule and details | User on Meds screen | 1. Tap '+' FAB<br>2. Enter name, dosage, time, instructions, category<br>3. Tap Add | Name: Aspirin<br>Dosage: 100mg<br>Time: 08:00 AM<br>Category: Morning | Medication appears in list and saves to local DB | Medication added & saved | **PASS** |
| **TC_WO_03** | Medication Editing | Verify editing existing medication details | Medication exists in list | 1. Tap Edit icon<br>2. Modify dosage or time<br>3. Tap Save | Updated dosage: 200mg | Medication card updates immediately with new details | Updated successfully | **PASS** |
| **TC_WO_04** | Medication Deletion | Verify deleting a medication removes it from the list | Medication exists in list | 1. Tap Delete icon | Selected medication ID | Medication is removed from active list | Removed successfully | **PASS** |
| **TC_WO_05** | Database Persistence | Verify data persists across app restart and power off/on | Medications added & deleted | 1. Restart app or device<br>2. Check medication list | Existing DB state | Deleted items do NOT reappear; all changes persist | Data preserved 100% | **PASS** |
| **TC_WO_06** | Alarm & Popup Trigger | Verify alarm rings and full-screen popup appears when time reaches | Pending medication scheduled | 1. Wait for system time to match OR tap Test Alarm | System time match | Alarm ringtone plays audibly; full-screen dialog pops up | Ringtone & dialog triggered | **PASS** |
| **TC_WO_07** | Take Medication Action | Verify marking medication as taken via alarm popup | Alarm popup is active | 1. Tap "Take Now & Stop Alarm" | Active alarm medication | Alarm stops; status updates to Taken; pill count decrements | Action executed successfully | **PASS** |
| **TC_WO_08** | Pill Refill Tracker | Verify low supply alert (< 7 pills) and refill action | Medication pill count <= 7 | 1. Go to Refills tab<br>2. Observe warning<br>3. Tap "Refill +30" | Pill count: 5 | Low supply highlighted; tapping refill adds 30 pills | Refilled successfully | **PASS** |
| **TC_WO_09** | Health Journal | Verify logging health notes and symptoms with timestamp | User on Refills tab | 1. Enter symptom note<br>2. Tap "Save Note" | Note: "Feeling well" | Note saves and displays in journal feed with timestamp | Logged successfully | **PASS** |
| **TC_WO_10** | Settings & Alarm Tones | Verify configuring alarm tones, sound, vibration, and contacts | User on Settings tab | 1. Change alarm tone<br>2. Toggle sound/vibration<br>3. Update caregiver contact | Tone: Gentle Chime<br>Contact: Dr. Smith | Settings update instantly and apply to system | Saved successfully | **PASS** |

---

## 4. DEFECT / BUG TRACKING SUMMARY
- **Total Defects Logged:** 0 critical, 0 major (all resolved during visual lint and build phases).
- **Resolution Status:** All automated tests and UI checks passed with zero compilation errors or accessibility violations.

---

## 5. CONCLUSION & SIGN-OFF
- **Total Test Cases Executed:** 10
- **Passed:** 10
- **Failed:** 0
- **Overall Result:** SUCCESSFUL / READY FOR DEPLOYMENT
- **Tested By:** QA Team / Blessings Chibwana Template Standard
