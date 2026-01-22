# APK Security QuickScan (ASQS)
- simple scanning tool to quickly assess a big number of apk files
- zero dependencies and java 8+ compatible
- scans can look at resource files in apks (such as the AndroidManifest.xml)
- does not decompile code for speed
- can compute entropy for files - used for e.g. the firmware scan
- can scan 31742 files in <2s on a MacBook

## Usage
```
java -jar target/apksec-quickscan-<version>.jar --scan-allows-cleartext-traffic '../apk/iotspotter_android/*'
# => ...
# => INFO: processed 31742 files in 1745ms
```
