package com.dpudov.answerphone.data;

import android.content.Context;
import android.text.format.DateUtils;

import com.dpudov.answerphone.data.network.LpServerResponse;
import com.dpudov.answerphone.model.Message;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by DPudov on 11.03.2016.
 * This class is for the VKSdk library initialization
 */
public class AppUtils {
    //borrowed from https://github.com/kioltk/messenger
    public static Object parseResult(String result) throws Exception {
        if (result != null)
            try {
                String jsonResponse = result;
                JSONObject response = new JSONObject(jsonResponse);
                if (response.has("failed")) {
                    throw new Exception();
                }
                LpServerResponse longpollResponse = new LpServerResponse();
                longpollResponse.setTs(response.optInt("ts"));
                JSONArray updates = response.optJSONArray("updates");
                for (int i = 0; i < updates.length(); i++) {
                    longpollResponse.add(optUpdate(updates.optJSONArray(i)));
                }
                return longpollResponse;
            } catch (Exception e) {
                return e;
            }
        return null;
    }

    //borrowed from https://github.com/kioltk/messenger
    private static Object optUpdate(JSONArray jsonUpdate) throws Exception {
        int type = jsonUpdate.getInt(0);
        switch (type) {
            case 4:
                return new Message(jsonUpdate);
            //break;
            case 6:
                //todo read messages
                break;
            case 7:
                // 6,$peer_id,$local_id — прочтение всех входящих сообщений с $peer_id вплоть до $local_id включительно
                // 7,$peer_id,$local_id — прочтение всех исходящих сообщений с $peer_id вплоть до $local_id включительно
                break;
            case 8:
//                return new LongpollOnline(jsonUpdate);
            case 9:
//                return new LongpollOffline(jsonUpdate);
            case 61:
//                return new LongpollTyping(jsonUpdate.getInt(1));
            case 62:
//                return new LongpollTyping(jsonUpdate.getInt(1),jsonUpdate.getInt(2));
        }
        return "unparsed update: " + jsonUpdate;
    }

    public static String getFriendlyTimestamp(Context context, long dateInMillis, int flags) {
        final long rightnow = System.currentTimeMillis();
        final long timediff = rightnow - dateInMillis;
        if (timediff < DateUtils.DAY_IN_MILLIS) {
            return DateUtils.formatDateTime(context, dateInMillis,
                    DateUtils.FORMAT_SHOW_TIME | flags);
        } else if (timediff < DateUtils.WEEK_IN_MILLIS) {
            return DateUtils.formatDateTime(context, dateInMillis,
                    DateUtils.FORMAT_SHOW_WEEKDAY | flags);
        } else if (timediff < DateUtils.YEAR_IN_MILLIS) {
            return DateUtils.formatDateTime(context, dateInMillis,
                    DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_NO_YEAR
                            | flags);
        } else {
            return DateUtils.formatDateTime(context, dateInMillis,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                            | DateUtils.FORMAT_NUMERIC_DATE | flags);
        }
    }
}
