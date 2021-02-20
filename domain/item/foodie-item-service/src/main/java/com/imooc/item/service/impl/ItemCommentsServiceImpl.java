package com.imooc.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.item.mapper.ItemsCommentsMapperCustom;
import com.imooc.item.pojo.vo.MyCommentVO;
import com.imooc.item.service.ItemCommentsService;
import com.imooc.pojo.PagedGridResult;
import com.imooc.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ItemCommentsServiceImpl extends BaseService implements ItemCommentsService {

    // TODO 抽取到订单域中
//    @Autowired
//    public OrderItemsMapper orderItemsMapper;
//
//    @Autowired
//    public OrdersMapper ordersMapper;
//
//    @Autowired
//    public OrderStatusMapper orderStatusMapper;

    @Autowired
    public ItemsCommentsMapperCustom itemsCommentsMapperCustom;

//    @Autowired
//    private Sid sid;

    // TODO 抽取到订单域中
//    @Transactional(propagation = Propagation.SUPPORTS)
//    @Override
//    public List<OrderItems> queryPendingComment(String orderId) {
//        OrderItems query = new OrderItems();
//        query.setOrderId(orderId);
//        return orderItemsMapper.select(query);
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    @Override
//    public void saveComments(String orderId, String userId,
//                             List<OrderItemsCommentBO> commentList) {
//
//        // 1. 保存评价 items_comments
//        for (OrderItemsCommentBO oic : commentList) {
//            oic.setCommentId(sid.nextShort());
//        }
//        Map<String, Object> map = new HashMap<>();
//        map.put("userId", userId);
//        map.put("commentList", commentList);
//        itemsCommentsMapperCustom.saveComments(map);
//
//        // 2. 修改订单表改已评价 orders
//        Orders order = new Orders();
//        order.setId(orderId);
//        order.setIsComment(YesOrNo.YES.type);
//        ordersMapper.updateByPrimaryKeySelective(order);
//
//        // 3. 修改订单状态表的留言时间 order_status
//        OrderStatus orderStatus = new OrderStatus();
//        orderStatus.setOrderId(orderId);
//        orderStatus.setCommentTime(new Date());
//        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
//    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyComments(String userId,
                                           Integer page,
                                           Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        PageHelper.startPage(page, pageSize);
        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public void saveComments(Map<String, Object> map) {
        itemsCommentsMapperCustom.saveComments(map);
    }
}
