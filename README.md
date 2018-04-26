# pH meter

### 1. Introduction
   1. This project demonstrates how to read pH measurements using TI-RTOS applications and send it over BLE to an Android app.
### 2. Motivation
   1. Our project goal is to assist farmers who constantly need to keep track on their water supply pH level.
   2. Our project not only eliminates the need to reopen the water tank to make the measurement, but also lets you know about the history of the pH level by sampling every 20 seconds, Once you have been connected to the TI board with your phone, All the historical measurements will be show on your mobile device screen as a graph and the measurements time based on your local time (there is no need to synchronize any clock on the TI board).
### 3. System schematics:

   <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/image6.png" alt="System schematics" height="200">

### 4. Live system setup:

   <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/image3.jpg" alt="Live system setup" height="300">

### 5. Example video

   <a href="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/exp_video.mp4" target="_blank"><img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/exp_video.png" alt="Example video" height="300" border="10" /></a>

### 6. Equipment
   1. #### Hardware:
      1. [TI CC1350 Launchpad](http://www.ti.com/tool/LAUNCHXL-CC1350-4 "TI CC1350 Launchpad")
      2. [Analog pH Meter Kit](https://www.dfrobot.com/product-1025.html "Analog pH Meter Kit")
      3. Android device
   2. #### Software:
      1. [Android studio](https://developer.android.com/studio/index.html "Android studio") - for creating and editing Android apps
      2. [TI Code composer studio](http://www.ti.com/tool/CCSTUDIO "TI Code composer studio") - for developing software on the TI LAUNCHXL-CC1350
   3. #### Images of the Gravity_pH_Sensor:

       <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Gravity_pH_Sensor_1.jpg" alt="Gravity pH Sensor" height="200">
       <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Gravity_pH_Sensor_2.jpg" alt="Gravity pH Sensor" height="200">
       <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Gravity_pH_Sensor_3.jpg" alt="Gravity pH Sensor" height="200">
       <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Gravity_pH_Sensor_4.jpg" alt="Gravity pH Sensor" height="200">

### 7. Development process
   1. #### Board <=> Phone communication:
      ##### 1. The communication protocol between the CC1350 board and the mobile phone has three steps.
      ##### 2. Step 1 – Initial communication:
         1. The phone send “1” to the board using write characteristic "0000fff3-0000-1000-8000-00805f9b34fb".
         2. The board change logical mode to be “sending data home”.
         3. The phone send read characteristic "0000fff2-0000-1000-8000-00805f9b34fb" to the board.
         4. The board send his current epoch time (started when the board turned on).
      ##### 3. Step 2 – Read measurements:
         1. The phone send “2” to the board using write characteristic.
         2. The phone send read characteristic in a loop,
         3. For each read request, the board will send two samples back, each on as follow: [4 little endian bytes of time, 1 bytes of zeros, 4 little endian bytes of measured value, 1 bytes of zeros].
         4. The phone keep asking for data until he received 10 bytes of zeros.
      ##### 4. Step 3 – Finish communication:
         1. The phone send “3” to the board using write characteristic.
         2. The board clean his buffer.
   2. #### Android application:
      ##### 1. Development process:
         1. We used Android Studio 3.1.1 for developing the application
         2. For implementing a BLE application we were assisted by [this example](https://github.com/bauerjj/Android-Simple-Bluetooth-Example "Example 1"), [this example](https://github.com/googlesamples/android-BluetoothLeGatt "Example 2"), and [this manual](https://www.allaboutcircuits.com/projects/how-to-communicate-with-a-custom-ble-using-an-android-app/ "this manual"). 
         3. We start our developing process by simply create a basic Android app and only then we add the Bluetooth scanning methods.
         4. After that we make our app to create a basic connection to the BT device that was selected from the list, after simultaneously debug this process at both the Android app and the TI board we was ready to move on,
         5. Our next step was to simply send data using the write characteristic from the phone to the board, and then send read characteristic and make sure we get the correct raw data as the board sent,
         6. From here it was much simpler, We upgrade the Android app to follow our predefined communication protocol and everything work as expected.
         7. We then added to the app the graph and the help screen.
      ##### 2. Application flow:
         1. The application have three parts,
         2. The first one is to constantly scan for BT devices and wait for user prompt.

            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Screenshot_1.png" alt="Step 1" height="400">
            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Screenshot_2.png" alt="Step 1" height="400">
            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Screenshot_3.png" alt="Step 1" height="400">
         
         3. Then the app switch activity and start to communicate with the board using the communication protocol as described in section 7.1.
         4. The third part is to display the data to the user using values table and graph with zoom in/out capability

            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Screenshot_4.png" alt="Step 3" height="400">
            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Screenshot_5.png" alt="Step 3" height="400">

         5. In addition to the application there is a help page with information about PH

            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Screenshot_6.png" alt="Help page" height="400">
            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/Screenshot_7.png" alt="Help page" height="400">

   3. #### TI-RTOS application:
      ##### 1. Development process:
         1. We used the TI in-house Code Composer 7.3.0, for compiling and debugging the board application.
         2. We based our TI application on two TI projects: Simple Peripheral BLE and an ADC project.
         3. We initially burned the BLE stack to the board, to enable it to run BLE apps. This was done one time and was not needed again.
         4. The original BLE application was very generic and we had to perform many changes and improvements to its design i.e change BLE characteristics, BLE tasks and the BLE callback functions.
         5. We integrated the BLE communication together with the ADC, as well as a continuous sampling mechanism.
      ##### 2. Application flow:
         1. The application works on 2 main TI tasks: one for the BLE application (SimpleBLEPeripheral_taskFxn) and one for sampling the PH meter using the ADC and for taking the sample time.
         2. The BLE tasks operates two main characteristics: one for reading from the board and getting the samples and one for writing a byte to the board to send commands.
         3. The BLE application works as a server, waiting for requests from the client, which is the mobile application. When the application gets a read request, it checks the attribute of the write characteristic. If the characteristic is “2”, it starts sending all the samples it has. If the characteristic is “1”, it only sends the current time as it knows it. If the characteristic is “3”, the app resets the sending “session”, by returning the next sampling buffer to send, to the start of the samples buffer.
         4. The application currently samples the PH every 20 seconds, for demonstration and debugging purposes, but optimally it would sample the PH meter every hour, as this is a relevant real life sampling rate. This means that with the current buffer, we would have a backwards memory of almost 11 days.
         5. The application also samples the current seconds since board init in every PH samples, adds it to the Epoch time since board init, then writes it to the memory buffer.
      ##### 3. Technical details:
         1. The BLE app saves 2560B of samples data. This size is sufficient to save enough samples backwards and to also not overwhelm the device’s memory.
         2. The integrated circuit of the PH meter:

            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/image2.png" alt="Integrated circuit of the PH meter" height="300">

         3. The PH meter:

            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/image5.png" alt="PH meter" width="500">
            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/image4.png" alt="PH meter" height="200">

         4. The PH meter takes 5V and GND inputs, both supplied by the TI board. Its output is an analog output, which we connected to the board in an ADC input.
         5. The PH meter samples output an analog value in mV, which converts to PH value with the following translation table:

            <img src="https://github.com/xqgex/TI_CC1350_BLE/blob/master/README_files/image1.png" alt="Translation table">

            The board performs a conversion between these voltages to the required PH value.
### 8. Our learning process
   1. #### Android:
      1. Working with Android BLE wasn’t as simple as we expect it to be, We had to learn a lot about how Bluetooth and BLE work, and then to understand how this work at an Android device, Google provide some BLE code snippets but they are very complicated and require high level of understanding.
      2. Another thing we had to deal with was how to build Android app, There isn’t any curse at the university that teach us that before so we had to learn it by using a lot of online tutorials and StackOverflow Q&A
   2. #### TI-RTOS:
      1. The biggest issue we had in the TI application was a conflict between sampling time using the <time.h> function “time()”. Apparently, sampling the current time through the BLE callback functions or the BLE task, causes the BLE communication to collapse after a very short period of time (no more than 20 seconds). We attempted to debug the issue in various ways (which haven’t worked :) ):
         1. We thought it might be a conflict between the interrupts of the BLE and the run of the time function, for instance if during calling the time function, a BLE interrupt occured and failed which caused the communication to collapse. We tried disabling outer interrupts, both HW and SW, while calling BLE functions and enveloping the time function, so their interrupts won’t conflict.
         2. We tried using different time modules, structs, and C libraries.
         3. Divided the time sampling and BLE communication into 2 different tasks.
         4. Burning the stack again to the board, also tried changing to a different stack.
         5. Prolonged every possible BLE timeout parameter, and changed other BLE parameters which we thought might interfere.
         6. We ended up solving this issue by sampling the current time from <time.h> in epoch format, only once, in the board init function in the board main function, before the BLE was even initiated. Then, we switched the time function with a native TI-RTOS function “AONRTCSecGet()” which samples a processor register, which saves the current seconds past the board initialization. This means we get the board init time in epoch, and we only add the seconds past since the board init to the time sample we got, by that getting the current time. Using the native function did not produce a conflict with the BLE.
      2. Another issue we had was changing the BLE function to suit our needs at both the Android app and the TI board. The BLE technology might be lightweight but the protocol which implements it is not as much. We had to learn how to create services which suited our needs and maintain a correct BLE communication. There are many ways to crash the BLE protocol, from nano second timeouts, to attributes overflows, to incorrect service addressing.
      3. Another part we had to learn was utilizing and calibrating the PH meter. The PH meter came with instructions in Chinese. We had to find suitable instructions in English online. Also, we had to learn how to calibrate it and perform tests to make sure it works correctly. Also, the PH meter calibration consists of two parts: one is using SW, by calibrating a general offset in sampling a neutral 7.0 PH liquid, measuring the offset measured and manually adding it to the output sample. Second was physically changing the gain potential of the PH meter integrated circuit, after sampling a 4.0 PH acidic liquid. We made sure that the PH meter was sensitive enough to measure the PH in our required range (6.5-7.5 PH).
### 9. How to start using this project
   1. #### First of all - Download this project or clone it using git command:
      1. `git clone https://github.com/xqgex/TI_CC1350_BLE.git`
   2. #### Prerequisites:
      1. Designed for Android versions 4.4 KitKat (API 19) to 8.1 Oreo (API 27)
      2. Tested on Android versions 6.0 Marshmallow (API 23) and 7.1.1 Nougat (API 25)
   3. #### To start the android application:
      1. Open Android Studio.
      2. Press “File” -> “Open” to load the project files
      3. Get your android device ready for [USB debugging](http://www.kingoapp.com/root-tutorials/how-to-enable-usb-debugging-mode-on-android.htm "USB debugging").
      4. Connect your android device with USB cable and confirm USB debugging.
      5. Download the app to your android device by “Run” -> “Run ‘App’ (Shift+F10)”
   4. #### To start the TI_RTOS application:
      1. Open CCS
      2. Connect your CC1350 launchpad.
      3. Compile and debug the stack example (only needs to be done once).
      4. Compile and debug our app.

