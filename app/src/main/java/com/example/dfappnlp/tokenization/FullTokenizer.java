package com.example.dfappnlp.tokenization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FullTokenizer {
    private final BasicTokenizer basicTokenizer;
    private final WordpieceTokenizer wordpieceTokenizer;
    private final Map<String, Integer> dic;

    public FullTokenizer(Map<String, Integer> inputDic, boolean doLowerCase) {
        dic = inputDic;
        basicTokenizer = new BasicTokenizer(doLowerCase);
        wordpieceTokenizer = new WordpieceTokenizer(inputDic);
    }

    public List<String> tokenize(String text) {
        List<String> splitTokens = new ArrayList<>();
        for (String token : basicTokenizer.tokenize(text)) {
            splitTokens.addAll(wordpieceTokenizer.tokenize(token));
        }
        return splitTokens;
    }

    public List<Integer> convertTokensToIds(List<String> tokens) {
        List<Integer> outputIds = new ArrayList<>();
        for (String token : tokens) {
            if (dic.containsKey(token)){
                outputIds.add(dic.get(token));
            }else{
                outputIds.add(0);
            }

        }
        return outputIds;
    }
}
