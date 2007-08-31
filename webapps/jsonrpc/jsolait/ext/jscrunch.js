Module("jscrunch", "0.0.1", function(thisMod){
    var lang=importModule("lang");
    
    thisMod.crunchCode=function(s){
        var t=new lang.Tokenizer(s);
        var tokens = lang.tokens;
        var r="";
        while(!t.finished()){
            var n = t.next();
            switch(n[0]){
                case tokens.DOCCOMMENT:
                case tokens.COMMENT:
                case tokens.WSP:
                    break;
                case tokens.NAME:
                    if(n[1] == "var" || n[1] == "const" || n[1] == "return"){
                        r+= n[1] + " ";
                    }else if(n[1] == "in"){
                        r+= " " + n[1] + " ";
                    }else{
                        r += n[1];
                    }
                    break;
                case tokens.NL:
                    if(r.charAt(r.length-1) != "\n" && r.length > 0){
                        r += n[1];
                    }
                    break;
                default:
                    r += n[1];
            }
        }
        return r;
    }
})
