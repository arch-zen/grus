/*
 * Copyright 2007-2021, CIIC Guanaitong, Co., Ltd.
 * All rights reserved.
 */

package com.ciicgat.api.core;

import com.ciicgat.api.core.contants.HeaderConstants;
import com.ciicgat.api.core.contants.VersionConstants;
import com.ciicgat.api.core.model.TestBean;
import com.ciicgat.api.core.service.HttpGetDeleteService;
import com.ciicgat.grus.json.JSON;
import com.ciicgat.sdk.lang.convert.ApiResponse;
import com.ciicgat.sdk.lang.convert.Pagination;
import com.ciicgat.sdk.lang.url.UrlCoder;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by August.Zhou on 2017/7/28 14:27.
 */

public class TestHttpGetDeleteService {
    private static MockWebServer mockWebServer;
    private static HttpGetDeleteService testService;

    @BeforeAll
    public static void init() {
        Pair<HttpGetDeleteService, MockWebServer> pair = TestUtil.newInstance("get-delete", HttpGetDeleteService.class);
        mockWebServer = pair.getRight();
        testService = pair.getLeft();
    }

    @AfterAll
    public static void stop() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    public synchronized void testGet() {
        TestBean bean1 = new TestBean("jasdlfj", 91823);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setResponseCode(200)
                .setHeader(HeaderConstants.ERROR_CODE_HEADER, 0)
                .setHeader(HeaderConstants.ERROR_MSG_HEADER, "")
                .setHeader(HeaderConstants.API_VERSION_HEADER, VersionConstants.V2)
                .setBody(JSON.toJSONString(bean1));
        mockWebServer.enqueue(mockResponse);

        TestBean bean = testService.get("我的xx@", 456);
        Assertions.assertEquals(bean1, bean);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //path encode的时候，
        Assertions.assertEquals("/get/" + UrlCoder.encode("我的xx@") + "?count=456", recordedRequest.getPath());
        Assertions.assertEquals("GET", recordedRequest.getMethod());
        String bodyString = recordedRequest.getBody().readUtf8();
        Assertions.assertEquals("", bodyString);
    }


    @Test
    public synchronized void testGetUnNormal() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> testService.getUnNormal("我的xx@", 456));
    }

    @Test
    public synchronized void testDelete() {
        TestBean bean1 = new TestBean("jasdlfj", 91823);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setResponseCode(200)
                .setHeader(HeaderConstants.ERROR_CODE_HEADER, 0)
                .setHeader(HeaderConstants.ERROR_MSG_HEADER, "")
                .setHeader(HeaderConstants.API_VERSION_HEADER, VersionConstants.V2)
                .setBody(JSON.toJSONString(bean1));
        mockWebServer.enqueue(mockResponse);

        TestBean bean = testService.delete("我的xx@", 456);
        Assertions.assertEquals(bean1, bean);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("/delete/" + UrlCoder.encode("我的xx@") + "?count=456", recordedRequest.getPath());
        Assertions.assertEquals("DELETE", recordedRequest.getMethod());
        String bodyString = recordedRequest.getBody().readUtf8();
        Assertions.assertEquals("", bodyString);
    }

    @Test
    public synchronized void testGetWithApiRespData() {
        TestBean bean1 = new TestBean("jasdlfj", 91823);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setResponseCode(200)
                .setHeader(HeaderConstants.ERROR_CODE_HEADER, 0)
                .setHeader(HeaderConstants.ERROR_MSG_HEADER, "")
                .setHeader(HeaderConstants.API_VERSION_HEADER, VersionConstants.V1)
                .setBody(ApiResponse.success(bean1).toString());
        mockWebServer.enqueue(mockResponse);

        TestBean bean = testService.getWithApiRespData();
        Assertions.assertEquals(bean1, bean);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("/getWithApiRespData", recordedRequest.getPath());
        Assertions.assertEquals("GET", recordedRequest.getMethod());
        String bodyString = recordedRequest.getBody().readUtf8();
        Assertions.assertEquals("", bodyString);
    }

    @Test
    public synchronized void testGetWithApiRespDataCodeNotZero() {
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setResponseCode(200)
                .setHeader(HeaderConstants.ERROR_CODE_HEADER, 222)
                .setHeader(HeaderConstants.ERROR_MSG_HEADER, "error")
                .setHeader(HeaderConstants.API_VERSION_HEADER, VersionConstants.V2)
                .setBody(ApiResponse.fail(222, "error").toString());
        mockWebServer.enqueue(mockResponse);
        BusinessFeignException e = null;
        TestBean bean = null;
        try {
            bean = testService.getWithApiRespDataCodeNotZero();
        } catch (BusinessFeignException e1) {
            e = e1;
        }

        Assertions.assertNotNull(e);
        Assertions.assertNull(bean);
        Assertions.assertEquals(222, e.getErrorCode());

        try {
            mockWebServer.takeRequest();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }

    }


    @Test
    public synchronized void testGetBeanList() {
        TestBean bean1 = new TestBean("我的你的阿历克斯江东父老；扩大社交分类；卡斯蒂洛；价格", 91823);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setResponseCode(200)
                .setHeader(HeaderConstants.ERROR_CODE_HEADER, 0)
                .setHeader(HeaderConstants.ERROR_MSG_HEADER, "")
                .setHeader(HeaderConstants.API_VERSION_HEADER, VersionConstants.V2)
                .setBody(JSON.toJSONString(Arrays.asList(bean1)));
        mockWebServer.enqueue(mockResponse);

        List<TestBean> beans = testService.getBeanList();
        Assertions.assertEquals(bean1, beans.get(0));

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("/getBeanList", recordedRequest.getPath());
        Assertions.assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    public synchronized void testGetBeanPagination() {
        TestBean bean1 = new TestBean("jasdlfj", 91823);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setResponseCode(200)
                .setHeader(HeaderConstants.ERROR_CODE_HEADER, 0)
                .setHeader(HeaderConstants.ERROR_MSG_HEADER, "")
                .setHeader(HeaderConstants.API_VERSION_HEADER, VersionConstants.V2)
                .setBody(JSON.toJSONString(new Pagination<TestBean>(1, Arrays.asList(bean1))));
        mockWebServer.enqueue(mockResponse);

        Pagination<TestBean> beans = testService.getBeanPagination();
        Assertions.assertEquals(bean1, beans.getDataList().get(0));

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("/getBeanPagination", recordedRequest.getPath());
        Assertions.assertEquals("GET", recordedRequest.getMethod());
    }


    @Test
    public synchronized void testGetApiResponseOfBeanList() {
        TestBean bean1 = new TestBean("jasdlfj", 91823);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setResponseCode(200)
                .setBody(ApiResponse.success(Arrays.asList(bean1)).toString());
        mockWebServer.enqueue(mockResponse);

        ApiResponse<List<TestBean>> bean = testService.getApiResponseOfBeanList();
        Assertions.assertEquals(bean1, bean.getData().get(0));

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("/getApiResponseOfBeanList", recordedRequest.getPath());
        Assertions.assertEquals("GET", recordedRequest.getMethod());

    }

    @Test
    public synchronized void testGetBeanListWithApiRespData() {
        TestBean bean1 = new TestBean("jasdlfj", 91823);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setResponseCode(200)
                .setHeader(HeaderConstants.ERROR_CODE_HEADER, 0)
                .setHeader(HeaderConstants.ERROR_MSG_HEADER, "")
                .setHeader(HeaderConstants.API_VERSION_HEADER, VersionConstants.V1)
                .setBody(ApiResponse.success(Arrays.asList(bean1)).toString());
        mockWebServer.enqueue(mockResponse);

        List<TestBean> bean = testService.getBeanListWithApiRespData();
        Assertions.assertEquals(bean1, bean.get(0));

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("/getBeanListWithApiRespData", recordedRequest.getPath());
        Assertions.assertEquals("GET", recordedRequest.getMethod());
    }


    @Test
    public synchronized void testGetWithListParams() {
        TestBean bean1 = new TestBean("jasdlfj", 91823);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .setHeader(HeaderConstants.ERROR_CODE_HEADER, 0)
                .setHeader(HeaderConstants.ERROR_MSG_HEADER, "")
                .setHeader(HeaderConstants.API_VERSION_HEADER, VersionConstants.V2)
                .setResponseCode(200)
                .setBody(JSON.toJSONString(bean1));
        mockWebServer.enqueue(mockResponse);


        TestBean bean = testService.getWithListParams(Arrays.asList("server1", "server2"), 456);
        Assertions.assertEquals(bean1, bean);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("/get?serverIds=server1&serverIds=server2&count=456", recordedRequest.getPath());
        Assertions.assertEquals("GET", recordedRequest.getMethod());
        String bodyString = recordedRequest.getBody().readUtf8();
        Assertions.assertEquals("", bodyString);
    }

}
