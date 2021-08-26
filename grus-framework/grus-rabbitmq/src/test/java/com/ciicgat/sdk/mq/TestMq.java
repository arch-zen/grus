/*
 * Copyright 2007-2020, CIIC Guanaitong, Co., Ltd.
 * All rights reserved.
 */

package com.ciicgat.sdk.mq;

import com.ciicgat.sdk.lang.threads.Threads;
import com.rabbitmq.client.BuiltinExchangeType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by August.Zhou on 2018-11-15 11:13.
 */
public class TestMq {

    private static String forTestExchangeName = "for-test-exchange";

    private static String forTestRoutingKeyA = "for-test-routingKey-A";

    private static String forTestRoutingKeyB = "for-test-routingKey-B";

    @Test
    public void test() throws Exception {
        TestTracing.registerJaegerTracer();
        List<String> msgList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            msgList.add("测试消息:" + Math.random());
        }

        MsgReceiver receiver = MsgReceiver
                .newBuilder()
                .setExchangeName(forTestExchangeName)
                .addBindQueue(forTestRoutingKeyA, "a")
                .addBindQueue(forTestRoutingKeyB, "b")
                .setPrefetchCount(1)
                .setParallelNum(4)
                .build();

        List<String> aReceives = new Vector<>();

        List<String> bReceives = new Vector<>();

        receiver.register((r, msg) -> {
            if (forTestRoutingKeyA.equals(r)) {
                aReceives.add(msg);
            } else if (forTestRoutingKeyB.equals(r)) {
                bReceives.add(msg);
            }
            return true;
        });


        MsgDispatcher dispatcher = MsgDispatcher
                .newBuilder()
                .setExchangeName(forTestExchangeName)
                .setExchangeType(BuiltinExchangeType.DIRECT)
                .setParallelNum(2)
                .build();


        for (String s : msgList) {
            dispatcher.sendMsg(s, forTestRoutingKeyA);
            dispatcher.sendMsg(s, forTestRoutingKeyB);
        }

        Threads.sleepSeconds(10);
        compare(msgList, aReceives);
        compare(msgList, bReceives);
    }

    private void compare(List<String> a, List<String> b) {
        Assert.assertEquals(a.size(), b.size());
        List<String> aa = new ArrayList<>(a);
        Collections.sort(aa);

        List<String> bb = new ArrayList<>(b);
        Collections.sort(bb);
        for (int i = 0; i < a.size(); i++) {
            Assert.assertEquals(aa.get(i), bb.get(i));
        }
    }
}
