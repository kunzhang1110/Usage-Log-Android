pandoc ./README.docx --to=gfm -o README.md --extract-media=_img
python ./convert.py
if %errorlevel% EQU 0 (pause) else 

