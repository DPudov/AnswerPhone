package com.dpudov.answerphone.util;

import java.util.HashSet;
import java.util.Iterator;


/**
 * Created by DPudov on 21.03.2016.
 * This class is for the VKSdk library initialization
 */
public class UserIDS_Util {
    public static String makeUserIdsFromHashSet(HashSet<Integer> userIds) {
        String result = "";
        StringBuilder stringBuffer = new StringBuilder(result);
        Iterator iterator = userIds.iterator();
        for (int i = 0; i < userIds.size(); i++) {
            if (i > 0)
                stringBuffer.append(",");
            stringBuffer.append(Integer.toString((Integer) iterator.next()));
        }
        result = stringBuffer.toString();
        return result;
    }

    public static String makeUserIdsFromIntArray(int[] userIds) {
        String result = "";
        StringBuilder stringBuilder = new StringBuilder(result);
        for (int i = 0; i < userIds.length; i++) {
            if(i>0)
                stringBuilder.append(",");
            stringBuilder.append(Integer.toString(userIds[i]));
        }
        result = stringBuilder.toString();
        return result;
    }
}
