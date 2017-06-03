# cbrTekStraktor
cbrTekStraktor is an application to automatically extract text from the text bubbles or speech balloons present in comic book reader files (CBR).  Its prime goal is to perform analysis on the texts of comic books. cbrTekStraktor can however also be used for scanlation or similar purposes.  

The application also enables to manually define text areas in CBR files. The application comprises a simple graphical editor for further processing the extracted text.

The text extraction is achieved by a combination of statistical and graphical processing operations. It is based on the following 3 major algorithms
•	Binarization of color images (Niblak and other methods)
•	Connected components
•	K-Means clustering

Apache Tesseract is used to perform Optical Character Recognition on the extracted text.

A subsequent version of the application will
•	integrate with translation software in order to provide automated translation of comic book texts.
•	Provide a mechanism to automatically re-inject translated text into the text balloons

