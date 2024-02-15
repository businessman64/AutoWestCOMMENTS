"# AutoWest" 
"# AutoWest" 
**Installation Steps for GUIDO Test Automation

1. Install GUIDO test automation.
2. Copy the "yash" folder to the "/opt/" directory:  
   ```
sudo cp -r “yash” “/opt/”  
   ```
3. Change the owner, group, and mode of the folder to "sysadmin" from "root":
   
   ```
   sudo chown sysadmin /opt/yash
   sudo chgrp sysadmin /opt/yash
   sudo chmod 755 /opt/yash
   sudo chmod 755|777 /opt/yash/Jarvis/

   ```

4. Check if "libopencv_java430.so" exists in "home/sysadmin/.Sikulix/SikulixLibs/". If it exists, perform the following steps: 

a.	copy libopencv_java340.so file from “opt/yash/lib/” folder to home/sysadmin/.Sikulix/SikulixLibs/ :
```
sudo cp  “/opt/yash/lib/ libopencv_java340.so”  “home/sysadmin/.Sikulix/SikulixLibs/”
```

   b. Rename "libopencv_java340.so" to "libopencv_java430.so" in the "yash" folder:
   
      ```
      mv “/opt/yash/libopencv_java340.so” “/opt/yash/libopencv_java430.so”
      ```

   b. Create a symbolic link to the library:
   
      ```
          sudo ln -s “home/sysadmin/.Sikulix/SikulixLibs/libopencv_java430.so” “/usr/lib/libopencv_java340.so”

      ```

5. Copy all the ".so.3.4" files inside "opencv3.4.0/lib" in the "yash" folder to "/usr/lib":
   
   ```
   sudo cp “/opt/yash/lib/*.so.3.4” “/usr/lib/”
   ```

6. Install "libopenblas" by running the following command:
   
   ```
   sudo apt-get install libopenblas-dev
   ```



