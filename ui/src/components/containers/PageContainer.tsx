import { Box } from "@mui/material";
import { CSSProperties } from "react";

interface PageContainerProps {
  children: React.ReactElement | React.ReactElement[];
  style?: CSSProperties;
  testId?: string;
}

export const PageContainer: React.FC<PageContainerProps> = ({
  children,
  style = {},
  testId = "",
}) => {
  return (
    <Box
      data-testid={testId}
      style={{
        ...style,
        height: "100%",
        width: "100%",
      }}
    >
      {children}
    </Box>
  );
};

export default PageContainer;
