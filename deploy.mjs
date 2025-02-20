import { GrpcTransport } from "@protobuf-ts/grpc-transport";
import { ChannelCredentials } from "@grpc/grpc-js";
import {
  DeployApplicationRequest,
  DeployServiceClient,
  VpcAlias,
  Service,
  Container,
  Component,
  ServiceProtocol,
  logMessages,
  GrpcStatusCode,
} from "@aidiv/pulumi-deploy";

const transport = new GrpcTransport({
  host: "internal-ad08dafb4378c479e8d4e3f6ad6c645b-1220029048.us-gov-east-1.elb.amazonaws.com:80",
  channelCredentials: ChannelCredentials.createInsecure(),
});
const client = new DeployServiceClient(transport);

const ecrImages = process.env["ECR_IMAGES"].split(",");
const frontendImage = ecrImages.find((image) => image.includes("frontend"));

const request = DeployApplicationRequest.create({
  vpc: VpcAlias.DEV,
  appName: "carvermatrix",
  secretKey: process.env["DEPLOYMENT_KEY"],
  components: [
    Component.create({
      name: "front-end",
      service: Service.create({
        externalPath: "/carvermatrix",
        ports: [
          {
            name: "front-end",
            exposedPort: 80,
            containerPort: 3000,
            public: true,
            protocol: ServiceProtocol.HTTP,
          },
        ],
      }),
      containers: [
        Container.create({
          name: "nextjs",
          image: frontendImage,
          ports: [3000],
        }),
      ],
    }),
  ],
});
const result = client.deployApplication(request);
logMessages(result);

result.responses.onMessage((e) => {
  if (e.code !== GrpcStatusCode.OK) {
    throw e.message;
  }
});
