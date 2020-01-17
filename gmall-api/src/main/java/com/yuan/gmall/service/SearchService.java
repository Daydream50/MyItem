package com.yuan.gmall.service;

import com.yuan.gmall.bean.PmsSearchParam;
import com.yuan.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {

    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
