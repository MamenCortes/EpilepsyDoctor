# Night Guardian — Doctor Application User Guide

The Night Guardian platform enables clinicians to remotely monitor epilepsy patients using ECG, accelerometer (ACC) signals, symptom tracking, and structured clinical data.
This guide explains how to use the Doctor Application step by step, following the workflow a doctor will typically perform during daily use.

# Summary of Doctor Capabilities

With the Doctor Application, you can:

* Connect securely to the Night Guardian Server
* Log in using authenticated corporate credentials
* Access all assigned patient records
* Navigate patient information and physiological data
* Review symptom calendars
* View ECG and ACC recordings with interactive tools
* Add clinical comments
* Access your own information

# User Interface Guide
## 1. Connecting to the Night Guardian Server

When launching the Doctor Application, a dialog box appears requesting the **Server IP address**.

<img src="img/server-ip.png" width="400">

### Requirements for a Successful Connection

* Ensure your computer is on the **same local network** as the server.
* If connection fails repeatedly, adjust or temporarily disable the **firewall** (as permitted by your organization).
* To find your computer’s IP:

  * **Windows:** Open *Command Prompt* → `ipconfig`
  * **Mac/Linux:** Open *Terminal* → `ifconfig`

Once the IP is introduced, you can click:

* **OK**
  * If the IP is correct, the login screen will appear.
  * If incorrect, an error message is shown.
* **Cancel**
  * The application closes immediately.

A successful server connection is required before proceeding to authentication.

## 2. Logging In

After connecting, you will be taken to the **Login Screen**.

<img src="img/logIn.png" width="500">

### **Account Access**

When you are granted access to the Night Guardian platform for the first time, the hospital’s **IT department** will provide you with:

* A **corporate email address** (ending in `@nightguardian.com`)
* A **temporary password**

You cannot create or register your own account.
Once you log in for the first time using the credentials provided, you will be able to **change your password** to one of your choice (minimum 8 characters and at least one special character). After updating your password, you may continue using the application normally.


### Change password
To change the password, introduce a valid email and then click **"Forgot you password?"**. Then introduce the new password and click **Save**. 

<img src="img/changePassword.png" width="500">

### Login
Enter:

* Your corporate **Night Guardian email** (`@nightguardian.com`)
* Your **password** (minimum 8 characters + at least one special character/number)

Then click **Log In**. The possible outcomes are: 

* **Successful login** → access to the Doctor Main Menu
* **Incorrect credentials / insufficient permissions** → error message. Retry entering a valid email and password. 

## 3. Main Menu Overview

After login, the **Doctor Main Menu** is displayed. It acts as the navigation hub for all medical features.

<img src="img/doctor-menu.png" width="500">
The available options are: 

#### **3.1 See my details**
Displays your profile details (name, email, department, speciality, contact details, etc.). You cannot change the information. If you need to change any details, please contact the **IT department**. 

<img src="img/doctor-info.png" width="500">

#### **3.2 Search Patients**

Opens the patient list assigned to you by the administrator.

#### **3.3 Log Out**

Ends the current session and returns to the login screen. The connection to the server won't be lost unless you close the App. 

## 4. Finding and Selecting Patients

Choose **Search Patients** to access the list of your patients.

<img src="img/search-patient.png" width="500">

* Enter a **surname** and press **Search**: The list filters matching patients.
* Press **Reset**: All assigned patients are shown again.

To open a patient’s profile, select a patient and click **Open File**.

## 5. Patient Profile Overview

The patient profile contains three main tabs:

#### 5.1 Details: Personal Information

Displays demographic and administrative details:

* Name and surname
* Date of birth
* Gender
* Contact details

This section provides basic context of the patient.

<img src="img/patient-info-1.png" width="500">

#### 5.2 Physiological Signal Recordings

Lists all ECG and ACC recordings performed by the patient. Each report contains an ECG and ACC recording. Here you can: 

* **Filter reports by date**
* **Reset** to display all originals
* Select a report and click **Open File** to access the **Signal Viewer**

<img src="img/patient-info-2.png" width="500">

#### 5.3 Symptom Calendar

Visualizes symptoms reported by the patient on a **monthly calendar**. Features: 

* **Colored squares** indicate symptoms
* Hover to display the symptom name
* A color legend identifies each symptom category
* A **month selector** allows browsing across months

This view helps identify symptom trends.
<img src="img/patient-info-3.png" width="500">

## 6. Signal Viewer: ECG and ACC Analysis

Selecting a recording opens the **Signal Viewer**.

To switch between signals you may toggle between:

* **ECG** (electrocardiogram) will be shown first by default
* **ACC** (accelerometer values averages between x, y and z axis)
* **Comments section**: allows doctors to annotate clinical observations. When clicking **Back To Menu**, comments are **automatically saved** to the server and associated with the patient’s recording.

To enhance readability, signals are:

* Filtered
* Centered at zero
* Normalized
* Displayed in 10 second windows

This preserves clinical shape and frequency information.
<img src="img/ecg-signal.png" width="700">
<img src="img/acc-signal.png" width="700">

#### Interaction Tools

The viewer includes advanced navigation and analysis controls:

* **Left/Right arrows:** move along the timeline
* **Mouse drag:** zoom into a selected signal region
* **Hover:** view precise amplitude values
* **Reset:** restore the default 10-second window and zoom
* **Right-click menu:** additional display options

These tools enable detailed inspection of arrhythmias, anomalies, motion artifacts, and seizures.

## 7. Handling Network Disruptions

If the server disconnects due to network issues, server shutdown or unexpected errors, the application will: 

1. Display an error message
2. Return to the **Server IP prompt**
3. Require reconnection before further use

<img src="img/conexion-error.png" width="500">
