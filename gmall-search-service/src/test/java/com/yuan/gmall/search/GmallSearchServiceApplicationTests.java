package com.yuan.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.bean.PmsSearchSkuInfo;
import com.yuan.gmall.bean.PmsSkuInfo;
import com.yuan.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 导入mysql的数据到elasticsearch
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

	@Reference
	SkuService skuService;// 查询mysql


	@Autowired
	JestClient jestClient;

	//点击运行
	@Test
	public void contextLoads() throws IOException {
		put();

		//get();
	}

	public void put() throws IOException {


		List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();

        // 查询mysql数据
		pmsSkuInfoList = skuService.getAllSku("61");

		// 转化为es的数据结构
		List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
			PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();

			//相当于多个set
			/*
			a,b为对象
			BeanUtils.copyProperties(a, b);
			BeanUtils是org.springframework.beans.BeanUtils， a拷贝到b
			BeanUtils是org.apache.commons.beanutils.BeanUtils，b拷贝到a
			 */
			BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);

			pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));


			pmsSearchSkuInfos.add(pmsSearchSkuInfo);

		}

		// 导入es
		for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
												//数据		     	//库名			 //表名				//主键
			Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("pmsSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();
			jestClient.execute(put);
		}

	}

	//得到

	public void get() throws IOException {

		// jest的dsl工具
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// bool
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		// filter
		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","43");
		boolQueryBuilder.filter(termQueryBuilder);
		// must
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","华为");
		boolQueryBuilder.must(matchQueryBuilder);
		// query
		searchSourceBuilder.query(boolQueryBuilder);
		// from
		searchSourceBuilder.from(0);
		// size
		searchSourceBuilder.size(20);
		// highlight
		searchSourceBuilder.highlight(null);

		String dslStr = searchSourceBuilder.toString();

		System.err.println(dslStr);


		// 用api执行复杂查询
		List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

		Search search = new Search.Builder(dslStr).addIndex("gmall0105").addType("PmsSkuInfo").build();

		SearchResult execute = jestClient.execute(search);

		List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);

		for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
			PmsSearchSkuInfo source = hit.source;

			pmsSearchSkuInfos.add(source);
		}

		System.out.println(pmsSearchSkuInfos.size());
	}



}
