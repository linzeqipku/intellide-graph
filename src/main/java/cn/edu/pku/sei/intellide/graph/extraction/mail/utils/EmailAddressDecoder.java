package cn.edu.pku.sei.intellide.graph.extraction.mail.utils;

/*
 * JIRA email address encoding algorithm:
 * 		(special characters are '@' and '.') 
 * 		the algorithm is to replace '@', '.' to ' at ' and ' dot ' respectively.
 *   
 * The class is used to decode email address from encoded result.
 */
public class EmailAddressDecoder {
    public static String decode(String encodedEmailAddr) {
        if (encodedEmailAddr == null || encodedEmailAddr.isEmpty()) {
            return encodedEmailAddr;
        }

        String decodeATRes = encodedEmailAddr.replaceAll(" at ", " @ ");//decode 'at' to '@'
        String decodeATandDOTRes = decodeATRes.replaceAll(" dot ", " . ");//decode 'dot' to '.'
        //re-decode 'dot' for special cases when sequential dots occur and there is just a blank between two 'dot'
        //e.g.,"jianbin dot dot wang dot at pku dot edu dot cn"
        decodeATandDOTRes = decodeATandDOTRes.replaceAll(" dot ", " . ");//decode 'dot' to '.'

        return decodeATandDOTRes.replaceAll(" ", "");
    }

    public static void main(String[] args) {
        String encodedMailAddr;

        encodedMailAddr = "jpountz at gmail dot com";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "deBakker_Bas at emc dot com";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "chris at die-schneider dot net";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "lpb+apache at focalpoint dot com";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "shutear at 126 dot com";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "jianbin dot wang at pku dot edu dot cn";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "jianbin dot  dot wang dot at pku dot edu dot cn";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "jianbin dot dot wang dot at pku dot edu dot cn";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "jianbin dot dot dot wang dot at pku dot edu dot cn";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));

        encodedMailAddr = "jianbin dot dot dot dot wang dot at pku dot edu dot cn";
        //System.out.println(encodedMailAddr + "\t:" + decode(encodedMailAddr));
    }
}
