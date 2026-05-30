update train_seats
set seat_label = lpad(carriage_no::text, 2, '0') || '车 ' || seat_no
where seat_label is distinct from lpad(carriage_no::text, 2, '0') || '车 ' || seat_no;
