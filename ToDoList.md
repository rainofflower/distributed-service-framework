1、consumer和provider之间多连接支持（由于单条tcp连接传输数据量有限，支持多连接可提高数据传输效率）
2、pipeline处理业务和chain中新增节点并发性能优化。目前chain中每个节点执行invoke时都会更新pipeline的state状态，
并且多个线程并发在同一条pipeline中时，其中一个线程完成一个节点的invoke方法，就会释放state状态，此时另一个线程可能还在处理业务，
如果同时有第三个线程准备修改pipeline节点（如加节点），
并且由于第一个线程释放了state，第三个线程是有可能抢在第二个线程执行chain中下一个节点invoke方法之前更新state,独占pipeline,
那未完成整个chain业务的第二个线程就会因此多执行一个节点。
解决思路：（1）pipeline中新增一个volatile int字段，记录pipeline并发处理的线程数，在开始执行head时将计数+1，执行完tail后将计数-1。
在需要修改pipeline节点，迁移线程池前，等待pipeline计数=0，再执行修改pipeline线程。（2）ReadWriteLock实现
3、负载均衡
4、集群容错
5、泛化调用
6、其它序列化支持
7、其它动态代理技术支持
8、其它注册中心实现
9、多注册中心实现
10、调用链跟踪实现
11、服务治理、监控实现