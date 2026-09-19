@echo off
chcp 65001 >nul
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp bin com.codealpha.stocktrading.StockTradingPlatform
pause
