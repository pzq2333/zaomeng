package com.kingpivot.api.controller.ApiMemberOrderController;

import com.google.common.collect.Maps;
import com.kingpivot.api.dto.memberOrder.MemberOrderDetailDto;
import com.kingpivot.api.dto.memberOrder.MemberOrderListDto;
import com.kingpivot.base.cart.service.CartService;
import com.kingpivot.base.cartGoods.model.CartGoods;
import com.kingpivot.base.cartGoods.service.CartGoodsService;
import com.kingpivot.base.config.RedisKey;
import com.kingpivot.base.config.UserAgent;
import com.kingpivot.base.goodsShop.model.GoodsShop;
import com.kingpivot.base.goodsShop.service.GoodsShopService;
import com.kingpivot.base.member.model.Member;
import com.kingpivot.base.memberOrder.model.MemberOrder;
import com.kingpivot.base.memberOrder.service.MemberOrderService;
import com.kingpivot.base.memberlog.model.Memberlog;
import com.kingpivot.base.support.MemberLogDTO;
import com.kingpivot.common.KingBase;
import com.kingpivot.common.jms.SendMessageService;
import com.kingpivot.common.jms.dto.memberLog.MemberLogRequestBase;
import com.kingpivot.common.util.Constants;
import com.kingpivot.common.utils.*;
import com.kingpivot.protocol.ApiBaseController;
import com.kingpivot.protocol.MessageHeader;
import com.kingpivot.protocol.MessagePacket;
import com.kingpivot.protocol.MessagePage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api")
@RestController
@Api(description = "会员订单管理接口")
public class ApiMemberOrderController extends ApiBaseController {
    @Resource
    private SendMessageService sendMessageService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private GoodsShopService goodsShopService;
    @Autowired
    private MemberOrderService memberOrderService;
    @Autowired
    private CartService cartService;
    @Autowired
    private KingBase kingBase;
    @Autowired
    private CartGoodsService cartGoodsService;

    @ApiOperation(value = "店铺商品生成订单", notes = "店铺商品生成订单")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "sessionID", value = "登录标识", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "goodsShopID", value = "店铺商品id", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "qty", value = "数量", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "objectFeatureItemID1", value = "对象特征选项1", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "contactName", value = "联系人", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "contactPhone", value = "联系电话", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "address", value = "地址", dataType = "String")})
    @RequestMapping(value = "/createMemberOrder")
    public MessagePacket createMemberOrder(HttpServletRequest request) {
        String sessionID = request.getParameter("sessionID");
        if (StringUtils.isEmpty(sessionID)) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }
        Member member = (Member) redisTemplate.opsForValue().get(String.format("%s%s", RedisKey.Key.MEMBER_KEY.key, sessionID));
        if (member == null) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }
        MemberLogDTO memberLogDTO = (MemberLogDTO) redisTemplate.opsForValue().get(String.format("%s%s", RedisKey.Key.MEMBERLOG_KEY.key, sessionID));
        if (memberLogDTO == null) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }

        String contactName = request.getParameter("contactName");
        if (StringUtils.isEmpty(contactName)) {
            return MessagePacket.newFail(MessageHeader.Code.contactNameIsNull, "联系人不能为空");
        }
        String contactPhone = request.getParameter("contactPhone");
        if (StringUtils.isEmpty(contactPhone)) {
            return MessagePacket.newFail(MessageHeader.Code.contactPhoneIsNull, "联系电话不能为空");
        }
        String address = request.getParameter("address");
        if (StringUtils.isEmpty(address)) {
            return MessagePacket.newFail(MessageHeader.Code.addressIsNull, "地址不能为空");
        }

        String goodsShopID = request.getParameter("goodsShopID");
        if (StringUtils.isEmpty(goodsShopID)) {
            return MessagePacket.newFail(MessageHeader.Code.goodsShopIdIsNull, "goodsShopID不能为空");
        }

        GoodsShop goodsShop = goodsShopService.findById(goodsShopID);
        if (goodsShop == null) {
            return MessagePacket.newFail(MessageHeader.Code.goodsShopIdIsError, "goodsShopID不正确");
        }

        String qty = request.getParameter("qty");

        if (StringUtils.isEmpty(qty)) {
            qty = "1";
        }

        String objectFeatureItemID1 = request.getParameter("objectFeatureItemID1");

        String memberOrderID = memberOrderService.createMemberOrder(member, goodsShop, objectFeatureItemID1, Integer.parseInt(qty), contactName, contactPhone, address);

        String description = String.format("%s店铺商品生成订单", member.getName());

        UserAgent userAgent = UserAgentUtil.getUserAgent(request.getHeader("user-agent"));
        MemberLogRequestBase base = MemberLogRequestBase.BALANCE()
                .sessionID(sessionID)
                .description(description)
                .userAgent(userAgent == null ? null : userAgent.getBrowserType())
                .operateType(Memberlog.MemberOperateType.CREATEMEMBERORDER.getOname())
                .build();

        sendMessageService.sendMemberLogMessage(JacksonHelper.toJson(base));

        Map<String, Object> rsMap = Maps.newHashMap();
        rsMap.put("memberOrderID", memberOrderID);

        return MessagePacket.newSuccess(rsMap, "createMemberOrder success!");
    }

    @ApiOperation(value = "购物车生成订单", notes = "购物车生成订单")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "sessionID", value = "登录标识", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "contactName", value = "联系人", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "contactPhone", value = "联系电话", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "address", value = "地址", dataType = "String")})
    @RequestMapping(value = "/createMemberOrderFromCart")
    public MessagePacket createMemberOrderFromCart(HttpServletRequest request) {
        String sessionID = request.getParameter("sessionID");
        if (StringUtils.isEmpty(sessionID)) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }
        Member member = (Member) redisTemplate.opsForValue().get(String.format("%s%s", RedisKey.Key.MEMBER_KEY.key, sessionID));
        if (member == null) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }
        MemberLogDTO memberLogDTO = (MemberLogDTO) redisTemplate.opsForValue().get(String.format("%s%s", RedisKey.Key.MEMBERLOG_KEY.key, sessionID));
        if (memberLogDTO == null) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }

        String contactName = request.getParameter("contactName");
        if (StringUtils.isEmpty(contactName)) {
            return MessagePacket.newFail(MessageHeader.Code.contactNameIsNull, "联系人不能为空");
        }
        String contactPhone = request.getParameter("contactPhone");
        if (StringUtils.isEmpty(contactPhone)) {
            return MessagePacket.newFail(MessageHeader.Code.contactPhoneIsNull, "联系电话不能为空");
        }
        String address = request.getParameter("address");
        if (StringUtils.isEmpty(address)) {
            return MessagePacket.newFail(MessageHeader.Code.addressIsNull, "地址不能为空");
        }

        String cartID = cartService.getCartIdByMemberID(member.getId());
        if (cartID == null) {
            kingBase.insertCart(member);
            return MessagePacket.newFail(MessageHeader.Code.illegalParameter, "购物车为空");
        }

        List<CartGoods> cartGoodsList = cartGoodsService.getCartGoodsListByCartID(cartID);
        if (cartGoodsList.isEmpty()) {
            return MessagePacket.newFail(MessageHeader.Code.illegalParameter, "购物车为空");
        }

        String memberOrderID = memberOrderService.createMemberOrderFromCart(cartGoodsList, member, contactName, contactPhone, address);

        String description = String.format("%s店铺商品生成订单", member.getName());

        UserAgent userAgent = UserAgentUtil.getUserAgent(request.getHeader("user-agent"));
        MemberLogRequestBase base = MemberLogRequestBase.BALANCE()
                .sessionID(sessionID)
                .description(description)
                .userAgent(userAgent == null ? null : userAgent.getBrowserType())
                .operateType(Memberlog.MemberOperateType.CREATEMEMBERORDER.getOname())
                .build();

        sendMessageService.sendMemberLogMessage(JacksonHelper.toJson(base));

        Map<String, Object> rsMap = Maps.newHashMap();
        rsMap.put("memberOrderID", memberOrderID);

        return MessagePacket.newSuccess(rsMap, "createMemberOrder success!");
    }

    @ApiOperation(value = "获取会员订单列表", notes = "获取会员订单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "sessionID", value = "登录标识", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "currentPage", value = "分页，页码从1开始", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageNumber", value = "每一页大小", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "status", value = "状态", dataType = "int")})
    @RequestMapping(value = "/getMemberOrderList")
    public MessagePacket getMemberOrderList(HttpServletRequest request) {
        String sessionID = request.getParameter("sessionID");
        if (StringUtils.isEmpty(sessionID)) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }
        Member member = (Member) redisTemplate.opsForValue().get(String.format("%s%s", RedisKey.Key.MEMBER_KEY.key, sessionID));
        if (member == null) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }
        MemberLogDTO memberLogDTO = (MemberLogDTO) redisTemplate.opsForValue().get(String.format("%s%s", RedisKey.Key.MEMBERLOG_KEY.key, sessionID));
        if (memberLogDTO == null) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("isValid", Constants.ISVALID_YES);
        paramMap.put("isLock", Constants.ISLOCK_NO);
        String status = request.getParameter("status");
        if (StringUtils.isNotBlank(status)) {
            paramMap.put("status", Integer.parseInt(status));
        }
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "createdTime"));
        Object currentPage = request.getParameter("currentPage");
        Object pageNumber = request.getParameter("pageNumber");

        TPage page = ApiPageUtil.makePage(currentPage, pageNumber);

        Pageable pageable = new PageRequest(page.getStart(), page.getPageSize(), new Sort(orders));

        Page<MemberOrder> rs = memberOrderService.list(paramMap, pageable);

        List<MemberOrderListDto> list = null;
        if (rs != null && rs.getSize() != 0) {
            list = BeanMapper.mapList(rs.getContent(), MemberOrderListDto.class);
            page.setTotalSize(rs.getSize());
        }

        String description = String.format("%s获取会员订单列表", member.getName());

        UserAgent userAgent = UserAgentUtil.getUserAgent(request.getHeader("user-agent"));
        MemberLogRequestBase base = MemberLogRequestBase.BALANCE()
                .sessionID(sessionID)
                .description(description)
                .userAgent(userAgent == null ? null : userAgent.getBrowserType())
                .operateType(Memberlog.MemberOperateType.GETMEMBERORDERLIST.getOname())
                .build();

        sendMessageService.sendMemberLogMessage(JacksonHelper.toJson(base));

        Map<String, Object> rsMap = Maps.newHashMap();
        MessagePage messagePage = new MessagePage(page, list);
        rsMap.put("data", messagePage);
        return MessagePacket.newSuccess(rsMap, "getMemberOrderList success!");
    }

    @ApiOperation(value = "获取会员订单详情", notes = "获取会员订单详情")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "sessionID", value = "登录标识", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "memberOrderID", value = "会员订单id", dataType = "String")})
    @RequestMapping(value = "/getMemberOrderDetail")
    public MessagePacket getMemberOrderDetail(HttpServletRequest request) {
        String sessionID = request.getParameter("sessionID");
        if (StringUtils.isEmpty(sessionID)) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }
        Member member = (Member) redisTemplate.opsForValue().get(String.format("%s%s", RedisKey.Key.MEMBER_KEY.key, sessionID));
        if (member == null) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }
        MemberLogDTO memberLogDTO = (MemberLogDTO) redisTemplate.opsForValue().get(String.format("%s%s", RedisKey.Key.MEMBERLOG_KEY.key, sessionID));
        if (memberLogDTO == null) {
            return MessagePacket.newFail(MessageHeader.Code.unauth, "请先登录");
        }

        String memberOrderID = request.getParameter("memberOrderID");
        if (StringUtils.isEmpty(memberOrderID)) {
            return MessagePacket.newFail(MessageHeader.Code.memberOrderIDIsNull, "memberOrderID为空");
        }

        MemberOrder memberOrder = memberOrderService.findById(memberOrderID);

        if (memberOrder == null) {
            return MessagePacket.newFail(MessageHeader.Code.memberOrderIDIsError, "memberOrderID不正确");
        }

        MemberOrderDetailDto memberOrderDetailDto = BeanMapper.map(memberOrder, MemberOrderDetailDto.class);

        String description = String.format("%s获取会员订单详情", member.getName());

        UserAgent userAgent = UserAgentUtil.getUserAgent(request.getHeader("user-agent"));
        MemberLogRequestBase base = MemberLogRequestBase.BALANCE()
                .sessionID(sessionID)
                .description(description)
                .userAgent(userAgent == null ? null : userAgent.getBrowserType())
                .operateType(Memberlog.MemberOperateType.GETMEMBERORDERDETAIL.getOname())
                .build();

        sendMessageService.sendMemberLogMessage(JacksonHelper.toJson(base));

        Map<String, Object> rsMap = Maps.newHashMap();
        rsMap.put("data", memberOrderDetailDto);
        return MessagePacket.newSuccess(rsMap, "getMemberOrderDetail success!");
    }
}
