@ECHO off
ECHO Compiling benchmark program...
SET VALE_COMPILER=C:\Users\theow\Desktop\Thesis\ValeCompiler-0.1.0.0-Win

CALL python3 %VALE_COMPILER%\valec.py %VALE_COMPILER%\vstl\list.vale %VALE_COMPILER%\vstl\hashmap.vale %VALE_COMPILER%\vstl\hashset.vale %VALE_COMPILER%\BenchmarkRL\src\*.vale --region-override resilient-v3 --output-dir %cd%\..\..\benchmark_out --elide-checks-for-known-live --print-mem-overhead
