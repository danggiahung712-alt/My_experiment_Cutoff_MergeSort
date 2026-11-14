@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

title [DU AN THI NGHIEM MERGESORT CUTOFF]

if not "%1"=="" (
    if /i "%1"=="test" goto do_test
    if /i "%1"=="quick" goto do_quick
    if /i "%1"=="full" goto do_full
    if /i "%1"=="analyze" goto do_analyze
    if /i "%1"=="all" goto do_all
)

:menu
cls
echo ===============================================================================
echo       HE THONG THUC NGHIEM XAC DINH NGUONG CUTOFF CHO MERGESORT (DSA 1.3 & 2.2)
echo ===============================================================================
echo.
echo   [1] Bien dich va chay Bo kiem thu tu dong (Unit Tests - 79.982 assertions)
echo   [2] Chay thu nghiem toc do nhanh (Quick Benchmark ~15s, an toan)
echo   [3] Chay lai toan bo thuc nghiem chuyen sau (Full Mode: 9.300 phep do, ~5-8 phut)
echo   [4] Chay phan tich thong ke va ve lai toan bo do thi (python analyze.py)
echo   [5] Chay tron goi tu A den Z (Test -> Quick Benchmark -> Analyze Plots)
echo   [0] Thoat chuong trinh
echo.
echo ===============================================================================
set /p choice="Nhap lua chon cua ban [0-5]: "

if "%choice%"=="1" goto do_test
if "%choice%"=="2" goto do_quick
if "%choice%"=="3" goto do_full
if "%choice%"=="4" goto do_analyze
if "%choice%"=="5" goto do_all
if "%choice%"=="0" goto do_exit

echo Lua chon khong hop le!
pause
goto menu

:do_compile
echo.
echo [*] Dang bien dich ma nguon Java...
if not exist out mkdir out
javac -encoding UTF-8 -d out src\prj\*.java
if %errorlevel% neq 0 (
    echo [!] Loi bien dich Java! Vui long kiem tra lai JDK.
    pause
    goto menu
)
echo [OK] Bien dich thanh cong!
exit /b 0

:do_test
call :do_compile
echo.
echo [*] Dang chay bo kiem thu don vi (Unit Tests)...
java -cp out prj.OptimizedMergeSortTest
pause
goto menu

:do_quick
call :do_compile
echo.
echo [*] Dang chay thu nghiem toc do nhanh (Quick Benchmark trong ~15 giay)...
java -cp out prj.CutoffExperimentBenchmark quick
echo.
echo [*] Phan tich ket qua quick...
python analyze.py results\cutoff_quick_summary.csv
pause
goto menu

:do_full
call :do_compile
echo.
echo [!] CHU Y: Che do Full Mode se thuc hien 9.300 phep do (5 quy mo N x 4 phan bo x 31 cutoffs x 15 trials).
echo Thoi gian uoc tinh: 5 - 8 phut tuy cau hinh may.
set /p confirm="Ban co chac chan muon chay lai toan bo? [Y/N]: "
if /i not "%confirm%"=="Y" goto menu
echo.
echo [*] Dang thuc thi Full Benchmark...
java -Xms3g -Xmx3g -cp out prj.CutoffExperimentBenchmark
echo.
echo [*] Cap nhat lai phan tich va do thi...
python analyze.py
pause
goto menu

:do_analyze
echo.
echo [*] Dang chay phan tich thong ke va ve lai do thi bang Python...
python analyze.py
pause
goto menu

:do_all
call :do_compile
echo.
echo [*] 1/3. Chay Unit Tests...
java -cp out prj.OptimizedMergeSortTest
if %errorlevel% neq 0 pause && goto menu
echo.
echo [*] 2/3. Chay Quick Benchmark...
java -cp out prj.CutoffExperimentBenchmark quick
echo.
echo [*] 3/3. Phan tich thong ke va ve do thi...
python analyze.py
echo.
echo ===============================================================================
echo [THANH CONG] Toan bo quy trinh kiem thu va do dac da hoan tat!
echo ===============================================================================
pause
goto menu

:do_exit
echo Cam on ban da su dung chuong trinh!
exit /b 0
