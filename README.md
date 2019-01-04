# Universal Quiz Generator

This Android App will generate any type of multiple-choices quiz from your data.

The data should be provided as CSV file located in `res/raw/words_text.csv`. Fields should be separated by `|`.

The file should contain a header line describing how the App should handle each column.

The syntax used by each field in the header line is `{Field1 Role}{Field1 Name}{Field1 Type}|{Field2 Role}{Field2 Name}{Field2 Type}`:

* The `field role` in the first pair of brackets explains how to handle the field when generating the Quiz. 
 * `SRC` means that the field can be used as a question. 
 * `DEST` means that the field can be used as an answer.
 * `SRC/DEST` means that the field can be used both as a question and an answer.
 * `CAT` for the record category.
 * `GRP` for the record group.
 
* The value in the second pair of brackets indicates the title of the field.

* The value in the thirs pair of brackets indicate the data type of the field.
 * `TXT` for text.
 * `AUDIO` for audio.
 * `IMG` for images.

An example is given below:

{SRC/DEST}{Vietnamský Text}{TXT}|{DEST}{Vietnamská Fonetika}{TXT}|{SRC/DEST}{Ceský Text}{TXT}|{SRC/DEST}{Vietnamská Výslovnost}{AUDIO}|{CAT}{Téma}{CAT}|{GRP}{Kategorie}{GRP}
ai|1|kdo|Lekce1_words_audio.mp3,6-2|Lekce 1|Slovní Zásoba
bác si|5,3|lekar, lekarka|Lekce1_words_audio.mp3,11-3|Lekce 1|Slovní Zásoba
b?nh vi?n|6,6|nemocnice|Lekce1_words_audio.mp3,17-2|Lekce 1|Slovní Zásoba

A corresponding App is available on [Google Play](https://play.google.com/store/apps/details?id=com.karewa.vietnamesepro)

## Screenshots

|---------------|---------------|---------------|
|[](screen1.jpg)|[](screen2.jpg)|[](screen3.jpg)|
|[](screen4.jpg)|[](screen5.jpg)|[](screen6.jpg)|
|[](screen7.jpg)|[](screen8.jpg)|[](screen9.jpg)|

