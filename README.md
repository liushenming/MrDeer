MrDeer
===

I will introduce the MrDeer in 3 steps:  

1.  What's MrDeer and where will you use MrDeer.  
+   How to get the MrDeer and how to use it.  
+   The future of the project and how to make contributions to the MrDeer.  

***
##First
### What's MrDeer and where will you use MrDeer.
MrDeer is a MarkDown translator writtern by Java.You can use MrDeer in your project to translate the markdown String to HTML-Format  String.  
For example,if you want to create a Editor App on Android,and you want your users to write notes in MarkDown-format,you can get the String the users write and then put them into MrDeer
's M2HTranslator and then call the translate() for a translated,HTML-Format String.Then,you can put the HTML-String in a WebView and the WebView will parse the HTML Element and show the web view to the users.


***
##Second
###How to get the MrDeer and how to use it.  
You can get the MrDeer's Source Code [here](https://github.com/liushenming/MrDeer/releases/tag/mrdeer_v0.9).  
Uncompress the .tar.gz or .zip and then open the MrDeer-mrdeer_v0.9/src.Then copy the com folder to your project src/ folder.
Now you can use the MrDeer:   
  
    M2HTranslator translater=new M2HTranslator(mdString);
    String string_html=translater.translate();

Just put the MarkDown String(mdString) to the constructor of M2HTranslator to create a M2HTranslator object and then call the tranlsate() method to get the translated,HTML-Format String.
If you want to translate a new String,just call the loadString() of the M2HTranslator:  

    translater.loadString(mdString2);
    
***
##Third
###The future of the project and how to make contributions to the MrDeer.  
I want to optimize the project and make the project better.I will rewrite the program in C++,python and javascript recently and you can help me find some bugs or just fix them directly.  
You can help me to write a MrDeer_js , MrDeer_py, Mrdeer_C++ too.
