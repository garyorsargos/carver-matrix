import axios from "axios";

export class Api {
  private roleSetter: React.Dispatch<React.SetStateAction<string[]>>;
  constructor(roleSetter: React.Dispatch<React.SetStateAction<string[]>>) {
    this.roleSetter = roleSetter;
  }

  public async getTestResponse(): Promise<string> {
    // Until the api exists for this, it remains commented //

    // const { data } = await axios.get("/api/test");
    // const roles: string[] = data.roles;
    // this.roleSetter(roles);
    const { data } = await axios.get("/api/health");
    console.log("This is it, Johnny Boi: ", data);
    this.roleSetter(["STARTER_ADMIN"]);
    return "Test Response";
  }
}
