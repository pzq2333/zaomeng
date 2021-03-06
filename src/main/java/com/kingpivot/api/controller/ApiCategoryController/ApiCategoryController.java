package com.kingpivot.api.controller.ApiCategoryController;

import com.google.common.collect.Maps;
import com.kingpivot.api.dto.category.NodeCategoryListDto;
import com.kingpivot.base.category.model.Category;
import com.kingpivot.base.category.service.CategoryService;
import com.kingpivot.common.util.Constants;
import com.kingpivot.common.util.TreeInfo;
import com.kingpivot.common.util.TreeInfoDTO;
import com.kingpivot.common.utils.ApiPageUtil;
import com.kingpivot.common.utils.BeanMapper;
import com.kingpivot.common.utils.TPage;
import com.kingpivot.protocol.ApiBaseController;
import com.kingpivot.protocol.MessageHeader;
import com.kingpivot.protocol.MessagePacket;
import com.kingpivot.protocol.MessagePage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api")
@RestController
@Api(description = "分类管理接口")
public class ApiCategoryController extends ApiBaseController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation(value = "获取分类列表", notes = "获取分类列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "rootID", value = "父级id", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "depth", value = "深度", dataType = "String")})
    @RequestMapping(value = "/getCategoryList")
    public MessagePacket getCategoryList(HttpServletRequest request) {
        String rootID = request.getParameter("rootID");
        if (StringUtils.isEmpty(rootID)) {
            return MessagePacket.newFail(MessageHeader.Code.illegalParameter, "rootID不能为空");
        }
        String depth = request.getParameter("depth");
        if (StringUtils.isEmpty(depth)) {
            depth = "1";
        }
        TreeInfoDTO<TreeInfo> data = categoryService.getTreeData(rootID, depth);
        Map<String, Object> map = Maps.newConcurrentMap();
        map.put("data", data == null ? "" : data);
        return MessagePacket.newSuccess(map);
    }

    @ApiOperation(value = "获取子集分类列表", notes = "获取子集分类列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "rootID", value = "父级id", dataType = "String")})
    @RequestMapping(value = "/getNodeCategoryList")
    public MessagePacket getNodeCategoryList(HttpServletRequest request) {
        String rootID = request.getParameter("rootID");
        if (StringUtils.isEmpty(rootID)) {
            return MessagePacket.newFail(MessageHeader.Code.illegalParameter, "rootID不能为空");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("isValid", Constants.ISVALID_YES);
        paramMap.put("isLock", Constants.ISLOCK_NO);
        paramMap.put("parentID", rootID);

        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "orderSeq"));

        Object currentPage = request.getParameter("currentPage");
        Object pageNumber = request.getParameter("pageNumber");

        TPage page = ApiPageUtil.makePage(currentPage, pageNumber);

        Pageable pageable = new PageRequest(page.getStart(), page.getPageSize(), new Sort(orders));

        Page<Category> rs = categoryService.list(paramMap, pageable);

        List<NodeCategoryListDto> list = null;
        if (rs != null && rs.getSize() != 0) {
            list = BeanMapper.mapList(rs.getContent(), NodeCategoryListDto.class);
            page.setTotalSize(rs.getSize());
        }
        Map<String, Object> rsMap = Maps.newHashMap();
        MessagePage messagePage = new MessagePage(page, list);
        rsMap.put("data", messagePage);

        return MessagePacket.newSuccess(rsMap, "getNodeCategoryList success!");
    }
}
