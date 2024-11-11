import { Box, Typography } from "@mui/material";
import { Component } from "react";
import CategoryGroup from "./categoryGroup";

interface TargetGroupProps {
  targetName: string;
}

export class TargetGroup extends Component<TargetGroupProps> {
  render() {
    const { targetName } = this.props;
    return (
      <Box>
        <Typography>{targetName}</Typography>
        <CategoryGroup category="Criticality" />
        <CategoryGroup category="Accessibility" />
        <CategoryGroup category="Recuperability" />
        <CategoryGroup category="Vulnerability" />
        <CategoryGroup category="Effect" />
        <CategoryGroup category="Recognizability" />
      </Box>
    );
  }
}

export default TargetGroup;
