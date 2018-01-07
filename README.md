# IxpRsCollector

The IxpRsCollector queries [PeeringDB](https://www.peeringdb.com) and [Euro-IX](https://www.euro-ix.net/) to collect the Autonomous System Numbers (ASNs) used by IXP for their peering networks and Route Servers.

In particulare, the program queries the PeeringDB [REST API](https://www.peeringdb.com/api/net?info_type=Route Server) for networks of type "Route Server" and extracts the ASN, and the Euro-IX [IXP Service Matrx](https://www.euro-ix.net/tools/ixp-service-matrix/) to extract the ASN and RS ASN for each IXP.

Collecting the IXP ASNs is useful in santizing the BGP paths when infering the AS relationships, because often the IXP ASN is leaked in a BGP path and it appears as an intermediate transit network between two IXP peers. 
For more details read the [paper](https://conferences.sigcomm.org/imc/2013/papers/imc039-luckieAemb.pdf) on CAIDA's relationship inference algorithm:

```
Matthew Luckie, Bradley Huffaker, Amogh Dhamdhere, Vasileios Giotsas, and kc claffy. 
AS relationships, customer cones, and validation. 
In Proceedings of the 2013 conference on Internet measurement conference (IMC '13)
```
