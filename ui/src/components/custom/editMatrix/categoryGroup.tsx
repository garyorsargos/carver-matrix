import {
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Typography,
} from "@mui/material";
import { Component } from "react";

interface CategoryDisplayProps {
  category: string;
}

interface CategoryGroupState {
  score: number;
}

export class CategoryGroup extends Component<
  CategoryDisplayProps,
  CategoryGroupState
> {
  constructor(props: CategoryDisplayProps) {
    super(props);
    this.state = {
      score: 1, // initial score value
    };
  }

  handleInputChange = (event: SelectChangeEvent<number>) => {
    const newScore = Number(event.target.value);
    this.setState({ score: newScore });
    console.log("Selected score:", newScore);
  };

  render() {
    const { category } = this.props;
    const { score } = this.state;
    return (
      <Box>
        <div>
          <Typography>{category}</Typography>
        </div>
        <FormControl fullWidth sx={{ mt: 2 }}>
          <InputLabel id="score-select-label">Score</InputLabel>
          <Select
            id="score-select"
            value={score}
            label="Score"
            onChange={this.handleInputChange}
            sx={{
              ".MuiSelect-select": {
                color: "black",
              },
            }}
          >
            <MenuItem value={1}>1</MenuItem>
            <MenuItem value={2}>2</MenuItem>
            <MenuItem value={3}>3</MenuItem>
            <MenuItem value={4}>4</MenuItem>
            <MenuItem value={5}>5</MenuItem>
          </Select>
        </FormControl>
      </Box>
    );
  }
}

export default CategoryGroup;
