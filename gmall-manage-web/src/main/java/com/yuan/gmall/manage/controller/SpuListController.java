package com.yuan.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.gmall.bean.*;
import com.yuan.gmall.service.SpuListService;
import com.yuan.gmall.util.PmsUploadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuListController {

    @Reference
    SpuListService spuListService;

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> attrInfoList(String catalog3Id) {
        List<PmsProductInfo> pmsProductInfo = spuListService.spuList(catalog3Id);
        return pmsProductInfo;
    }

    /**
     * 保存功能
     *
     * @param pmsProductInfo
     * @return
     */
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        spuListService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    /**
     * 上传功能
     *
     * @param multipartFile
     * @return
     */
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        // 将图片或者音视频上传到分布式的文件存储系统
        // 将图片的存储路径返回给页面
        String file = PmsUploadUtil.uploadFile(multipartFile);
        return file;
    }

    /**
     * 销售属性
     *
     * @param spuId
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrLis(String spuId) {

        List<PmsProductSaleAttr> file = spuListService.saleAttrList(spuId);
        return file;
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId) {

        List<PmsProductImage> pmsProductImage = spuListService.fileUpload(spuId);

        return pmsProductImage;
    }
}
