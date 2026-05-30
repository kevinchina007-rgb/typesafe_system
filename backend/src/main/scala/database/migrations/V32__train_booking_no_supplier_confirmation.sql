update order_line_items
set item_status = 'Active',
    supplier_review_status = 'NotSubmitted'
where item_kind = 'Train';

update orders
set total_price_amount = (
        select coalesce(sum(booked_amount), 0)
        from order_line_items
        where order_id = orders.order_id
    ),
    remaining_refundable_amount = (
        select coalesce(sum(booked_amount), 0)
        from order_line_items
        where order_id = orders.order_id
    )
where exists (
    select 1
    from order_line_items
    where order_id = orders.order_id
      and item_kind = 'Train'
);
